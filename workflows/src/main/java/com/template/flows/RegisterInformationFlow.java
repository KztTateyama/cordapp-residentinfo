package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ResidentInformationContract;
import com.template.states.ResidentInformationState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
public class RegisterInformationFlow{

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final ProgressTracker progressTracker = new ProgressTracker();

        public final String     residentName;
        public final String     myNumber;
        public final Party      currentCity;
        public final String     currentAddress;
        public final String     birthday;
        public final String     oldAddress;

        public InitiatorFlow(String residentName, String myNumber, Party  currentCity, String  currentAddress, String birthday) {
            this.residentName      = residentName;
            this.myNumber          = myNumber;
            this.currentCity       = currentCity;
            this.currentAddress    = currentAddress;
            this.birthday          = birthday;
            this.oldAddress        = null;
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

            // Step Create outputState
            ResidentInformationState residentA =
                    new ResidentInformationState(
                            residentName,
                            myNumber,
                            currentCity,
                            currentAddress,
                            birthday,
                            oldAddress
                    );

            // Step 2. Create a new issue command.
            // Remember that a command is a CommandData object and a list of CompositeKeys

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

            // Step 6. The second argument of FinalityFlow should be empty,
            // as no signatures on other nodes are needed.
            List<FlowSession> sessions = Collections.emptyList();
            return subFlow(new FinalityFlow(ptx, sessions));

        }
    }
}
