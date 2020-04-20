package com.template.contracts;

import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
import com.template.states.ResidentInformationState;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ************
// * Contract *
// ************
//@LegalProseReference(uri = "<prose_contract_uri>")
public class ResidentInformationContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String IOU_CONTRACT_ID = "com.template.contracts.ResidentInformationContract";

    /**
     * The IOUContract can handle three transaction types involving [ResidentInformationState]s.
     * - RegisterInformation: Issuing a new [ResidentInformationState] on the ledger.
     * - ChangeInformation: Re-assigning the currentCity.
     * - DeleteInformation: deleting  [ResidentInformationState] on the ledger.
     */
    public interface Commands extends CommandData {
        class RegisterInformation extends TypeOnlyCommandData implements Commands{}
        class ChangeInformation extends TypeOnlyCommandData implements Commands{}
        class DeleteInformation extends TypeOnlyCommandData implements Commands{}
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        // We can use the requireSingleCommand function to extract command data from transaction.
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        String commandName = commandData.getClass().getSimpleName();

        if (commandData.equals(new Commands.RegisterInformation())) {

            requireThat(require -> {

                require.using("No inputs should be consumed when issuing an IOU.", tx.getInputStates().size() == 0);
                require.using( "Only one output state should be created when issuing an IOU.", tx.getOutputStates().size() == 1);

                ResidentInformationState outputState = tx.outputsOfType(ResidentInformationState.class).get(0);
                require.using( "A newly issued RegisterInformation must have a resident Name.", outputState.getResidentName() != null);
                require.using( "A newly issued RegisterInformation must have a myNumber.", outputState.getMyNumber() != null);
                require.using( "A newly issued RegisterInformation must have a birthday.", outputState.getBirthday() != null);
                require.using( "A newly issued RegisterInformation must have a current address.", outputState.getCurrentAddress() != null);

                final List<PublicKey> requiredSigners = command.getSigners();
                final List<PublicKey> expectedSigners = outputState.getParticipants()
                        .stream().map(AbstractParty::getOwningKey)
                        .collect(Collectors.toList());

                // Verifies right number of signers are required in the command
                if ( requiredSigners.size() < expectedSigners.size()) {
                    throw new IllegalArgumentException(String.format("%s requires exactly %d signers.", commandName, expectedSigners.size()));
                }

                // Verifies required signers covers all participants in the purchase order
                if( !requiredSigners.containsAll(expectedSigners) ) {
                    throw new IllegalArgumentException(String.format("%s requires signatures from all contract participants."));
                }

                return null;
            });


        }
        else if (commandData.equals(new Commands.ChangeInformation())) {

            requireThat(require -> {

                require.using("An IOU transfer transaction should only consume one input state.", tx.getInputStates().size() == 1);
                require.using("An IOU transfer transaction should only create one output state.", tx.getOutputStates().size() == 1);

                // Copy of input with new currentCity;
                ResidentInformationState inputState = tx.inputsOfType(ResidentInformationState.class).get(0);
                ResidentInformationState outputState = tx.outputsOfType(ResidentInformationState.class).get(0);
                ResidentInformationState checkOutputState = outputState.withNewCurrentCity(inputState.getCurrentCity(),inputState.getCurrentAddress());

                require.using("residentName is same between input state and output state.",
                        outputState.residentName.equals(inputState.residentName));

                require.using("myNumber is same between input state and output state.",
                        outputState.myNumber.equals(inputState.myNumber));

                require.using("birthday is same between input state and output state.",
                        outputState.birthday.equals(inputState.birthday));

                require.using("input state's currentAddress and output state's oldAddress must be equals.",
                        inputState.currentAddress.equals(outputState.oldAddress));

                require.using("input state's currentAddress and output state's currentAddress must change.",
                        !inputState.currentAddress.equals(outputState.currentAddress));

                require.using("The currentCity property must change in a transfer.", !outputState.currentCity.getOwningKey().equals(inputState.currentCity.getOwningKey()));

                List<PublicKey> listOfPublicKeys = new ArrayList<>();
                listOfPublicKeys.add(inputState.currentCity.getOwningKey());
                listOfPublicKeys.add(checkOutputState.currentCity.getOwningKey());

                Set<PublicKey> listOfParticipantPublicKeys = inputState.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toSet());
                listOfParticipantPublicKeys.add(outputState.currentCity.getOwningKey());

                List<PublicKey> arrayOfSigners = command.getSigners();
                Set<PublicKey> setOfSigners = new HashSet<PublicKey>(arrayOfSigners);
                require.using("The borrower, old lender and new lender only must sign an IOU transfer transaction", setOfSigners.equals(listOfParticipantPublicKeys) && setOfSigners.size() == 2);

                return null;
            });


        }
        else if (commandData.equals(new Commands.DeleteInformation())) {

            requireThat(require -> {

                ResidentInformationState inputState = tx.inputsOfType(ResidentInformationState.class).get(0);

                List<PublicKey> listOfPublicKeys = new ArrayList<>();
                listOfPublicKeys.add(inputState.currentCity.getOwningKey());
                Set<PublicKey> setOfSigners = new HashSet<PublicKey>(listOfPublicKeys);
                require.using("Both lender and borrower must sign IOU settle transaction.", setOfSigners.equals(listOfPublicKeys));

                return null;
            });

        }

    }

}