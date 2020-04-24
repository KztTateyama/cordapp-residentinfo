package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ResidentInformationContract;
import com.template.states.ResidentInformationState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// ******************
// * Initiator flow *
// ******************
public class DeleteInformationFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final ProgressTracker progressTracker = new ProgressTracker();

        private final UniqueIdentifier stateLinearId;
        private final Party currentCity;

        public InitiatorFlow(UniqueIdentifier stateLinearId,Party currentCity) {
            this.stateLinearId = stateLinearId;
            this.currentCity = currentCity;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            // 1. Retrieve the IOU State from the vault using LinearStateQueryCriteria
            List<UUID> listOfLinearIds = new ArrayList<>();
            listOfLinearIds.add(stateLinearId.getId());
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(null, listOfLinearIds);

            // 2. Get a reference to the inputState data that we are going to settle.
            Vault.Page results = getServiceHub().getVaultService().queryBy(ResidentInformationState.class, queryCriteria);
            StateAndRef inputStateAndRefToChange = (StateAndRef) results.getStates().get(0);

            // Step 1. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step 2. Create a new issue command.
            final Command<ResidentInformationContract.Commands.DeleteInformation> deleteCommand = new Command<>(
                    new ResidentInformationContract.Commands.DeleteInformation(),currentCity.getOwningKey());

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addInputState(inputStateAndRefToChange);
            builder.addCommand(deleteCommand);

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            // Step 6. The second argument of FinalityFlow should be empty,
            // as no signatures on other nodes are needed.
            List<FlowSession> sessions = Collections.emptyList();
            return subFlow(new FinalityFlow(ptx, sessions));

        }
    }
}
