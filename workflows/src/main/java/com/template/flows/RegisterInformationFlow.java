package com.template.flows;

//import com.google.common.collect.ImmutableList; <- cause of Gradle error ?
import co.paralleluniverse.fibers.Suspendable;
import com.template.states.ResidentInformationState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.template.contracts.ResidentInformationContract;

// ******************
// * Initiator flow *
// ******************
public class RegisterInformationFlow{

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final ProgressTracker progressTracker = new ProgressTracker();

        private final ResidentInformationState residentA;

        public InitiatorFlow(ResidentInformationState resident) {
            this.residentA = resident;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Step 1. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step 2. Create a new issue command.
            // Remember that a command is a CommandData object and a list of CompositeKeys
//            final Command<ResidentInformationContract.Commands.RegisterInformation> registerCommand = new Command<>(
//                    new ResidentInformationContract.Commands.RegisterInformation(), residentA.getParticipants()
//                    .stream().map(AbstractParty::getOwningKey)
//                    .collect(Collectors.toList()));

            final Command<ResidentInformationContract.Commands.RegisterInformation> registerCommand = new Command<>(
                    new ResidentInformationContract.Commands.RegisterInformation(),residentA.getCurrentCity().getOwningKey());

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
            builder.addOutputState(residentA, ResidentInformationContract.IOU_CONTRACT_ID);
            builder.addCommand(registerCommand);

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);


              // Step 6. Collect the other party's signature using the SignTransactionFlow.
//            List<Party> otherParties = residentA.getParticipants()
//                    .stream().map(el -> (Party) el)
//                    .collect(Collectors.toList());

//            otherParties.remove(getOurIdentity());

//            SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));
//
//            // Step 7. Assuming no exceptions, we can now finalise the transaction
//            return subFlow(new FinalityFlow(stx, sessions));

//            List<FlowSession> sessions = !getServiceHub().getMyInfo().isLegalIdentity(notary)
//                    ? Collections.singletonList(initiateFlow(notary)):Collections.emptyList();

//            return subFlow(new FinalityFlow(ptx, ImmutableList.of()));

//            List<FlowSession> sessions = Collections.emptyList();   // <- error

//            return subFlow(new FinalityFlow(ptx, sessions));        // <- error
            return subFlow(new FinalityFlow(ptx));                // <- normal

        }
    }
}
