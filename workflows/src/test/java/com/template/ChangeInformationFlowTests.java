package com.template;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.finance.Currencies;
import net.corda.testing.node.*;

//import com.template.flows.RegisterInformationFlow;
import com.template.flows.RegisterInformationFlow;
import com.template.flows.ChangeInformationFlow;
import com.template.states.ResidentInformationState;
import com.template.contracts.ResidentInformationContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class ChangeInformationFlowTests {

    private final SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

    private MockNetwork mockNetwork;
    private StartedMockNode a, b, c;

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters().withCordappsForAllNodes(
                Arrays.asList(
                        TestCordapp.findCordapp("com.template.flows"),
                        TestCordapp.findCordapp("com.template.contracts")
                )
        ).withNetworkParameters(testNetworkParameters(Collections.emptyList(), 4));
        mockNetwork = new MockNetwork(mockNetworkParameters);
        System.out.println(mockNetwork);

        a = mockNetwork.createNode(new MockNodeParameters());
        b = mockNetwork.createNode(new MockNodeParameters());
        c = mockNetwork.createNode(new MockNodeParameters());

        ArrayList<StartedMockNode> startedNodes = new ArrayList<>();
        startedNodes.add(a);
        startedNodes.add(b);
        startedNodes.add(c);

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach(el -> el.registerInitiatedFlow(ChangeInformationFlow.Responder.class));
        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private SignedTransaction issueIOU(ResidentInformationState iouState) throws InterruptedException, ExecutionException {
        RegisterInformationFlow.InitiatorFlow flow = new RegisterInformationFlow.InitiatorFlow(iouState);
        CordaFuture future = b.startFlow(flow);
        mockNetwork.runNetwork();
        return (SignedTransaction) future.get();
    }

    /**
     * Task 1.
     * Build out the beginnings of [ChangeInformationFlow]!
     * TODO: Implement the [ChangeInformationFlow] flow which builds and returns a partially [SignedTransaction].
     * Hint:
     * - This flow will look similar to the [RegisterInformationFlow].
     * - This time our transaction has an input state, so we need to retrieve it from the vault!
     * - You can use the [getServiceHub().getVaultService().queryBy(Class, queryCriteria)] method to get the latest linear states of a particular
     *   type from the vault. It returns a list of states matching your query.
     * - Use the [UniqueIdentifier] which is passed into the flow to create the appropriate Query Criteria.
     * - Use the [ResidentInformationState.withNewLender] method to create a copy of the state with a new lender.
     * - Create a Command - we will need to use the Transfer command.
     * - Remember, as we are involving three parties we will need to collect three signatures, so need to add three
     *   [PublicKey]s to the Command's signers list. We can get the signers from the input IOU and the new IOU you
     *   have just created with the new lender.
     * - Verify and sign the transaction as you did with the [RegisterInformationFlow].
     * - Return the partially signed transaction.
     */
    @Test
    public void flowReturnsCorrectlyFormedPartiallySignedTransaction() throws Exception {

        try{

            Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
            Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
            Party CCity = c.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
            String newAddress = "CCity DDD-EEE";

            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity,
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity,
                            "ACity XXX-YYY"
                    );

            SignedTransaction stx = issueIOU(iou);
            ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
//            ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),inputIou.getCurrentCity(),inputIou.currentAddress);
            ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),CCity,newAddress);
            Future<SignedTransaction> future = c.startFlow(flow);

            mockNetwork.runNetwork();

            SignedTransaction ptx = future.get();

            // Check the transaction is well formed...
            // One output ResidentInformationState, one input state reference and a Transfer command with the right properties.
            assert (ptx.getTx().getInputs().size() == 1);
            assert (ptx.getTx().getOutputs().size() == 1);
            assert (ptx.getTx().getOutputs().get(0).getData() instanceof ResidentInformationState);
            assert (ptx.getTx().getInputs().get(0).equals(new StateRef(stx.getId(), 0)));

            ResidentInformationState outputIOU = (ResidentInformationState) ptx.getTx().getOutput(0);
            Command command = ptx.getTx().getCommands().get(0);

            assert (command.getValue().equals(new ResidentInformationContract.Commands.ChangeInformation()));
            ptx.verifySignaturesExcept(b.getInfo().getLegalIdentities().get(0).getOwningKey(), c.getInfo().getLegalIdentities().get(0).getOwningKey(), mockNetwork.getDefaultNotaryIdentity().getOwningKey());

        } catch (ParseException e){
            e.printStackTrace();
        }

    }


        /**
        * Task 2.
        * We need to make sure that only the current lender can execute this flow.
        * TODO: Amend the [ChangeInformationFlow] to only allow the current lender to execute the flow.
        * Hint:
        * - Remember: You can use the node's identity and compare it to the [Party] object within the [ResidentInformationState] you
        *   retrieved from the vault.
        * - Throw an [IllegalArgumentException] if the wrong party attempts to run the flow!
        */
       @Test
       public void flowCanOnlyBeRunByCurrentLender() throws Exception {

           try{

               Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
               Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

               ResidentInformationState iou =
                       new ResidentInformationState(
                               "Yamada Taro",
                               "123456789012",
                               BCity,
                               "BCity YYY-ZZZ",
                               sdformat.parse("1990-02-10"),
                               ACity,
                               "ACity XXX-YYY"
                       );

               SignedTransaction stx = issueIOU(iou);
               ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
               ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),c.getInfo().component2().get(0).getParty(),inputIou.currentAddress);
               Future<SignedTransaction> future = a.startFlow(flow);
               try {
                   mockNetwork.runNetwork();
                   future.get();
               } catch (Exception exception) {
                   assert exception.getMessage().equals("java.lang.IllegalArgumentException: This flow must be run by the current lender.");
               }
           } catch (ParseException e){
               e.printStackTrace();
           }

       }

//       /**
//        * Task 3.
//        * Check that an [ResidentInformationState] cannot be transferred to the same lender.
//        * TODO: You shouldn't have to do anything additional to get this test to pass. Belts and Braces!
//        */
//       @Test
//       public void iouCannotBeTransferredToSameParty() throws Exception {
//           Party lender = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           Party borrower = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           SignedTransaction stx = issueIOU(new ResidentInformationState(Currencies.DOLLARS(10), lender, borrower));
//           ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
//           ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(), c.getInfo().component2().get(0).getParty());
//           Future<SignedTransaction> future = a.startFlow(flow);
//           try {
//               mockNetwork.runNetwork();
//               future.get();
//           } catch (Exception exception) {
//               System.out.println(exception.getMessage());
//               assert exception.getMessage().equals("Contract verification failed: Failed requirement: The lender property must change in a transfer.");
//           }
//       }
//
//       /**
//        * Task 4.
//        * Get the borrowers and the new lenders signatures.
//        * TODO: Amend the [ChangeInformationFlow] to handle collecting signatures from multiple parties.
//        * Hint: use [initiateFlow] and the [CollectSignaturesFlow] in the same way you did for the [RegisterInformationFlow].
//        */
//       @Test
//       public void flowReturnsTransactionSignedBtAllParties() throws Exception {
//           Party lender = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           Party borrower = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           SignedTransaction stx = issueIOU(new ResidentInformationState(Currencies.DOLLARS(10), lender, borrower));
//           ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
//           ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(), lender);
//           Future<SignedTransaction> future = a.startFlow(flow);
//           try {
//               mockNetwork.runNetwork();
//               future.get();
//               stx.verifySignaturesExcept(mockNetwork.getDefaultNotaryIdentity().getOwningKey());
//           } catch (Exception exception) {
//               System.out.println(exception.getMessage());
//           }
//       }
//
//       /**
//        * Task 5.
//        * We need to get the transaction signed by the notary service
//        * TODO: Use a subFlow call to the [FinalityFlow] to get a signature from the lender.
//        */
//       @Test
//       public void flowReturnsTransactionSignedByAllPartiesAndNotary() throws Exception {
//           Party lender = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           Party borrower = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
//           SignedTransaction stx = issueIOU(new ResidentInformationState(Currencies.DOLLARS(10), lender, borrower));
//           ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
//           ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(), c.getInfo().component2().get(0).getParty());
//           Future<SignedTransaction> future = a.startFlow(flow);
//           try {
//               mockNetwork.runNetwork();
//               future.get();
//               stx.verifyRequiredSignatures();
//           } catch (Exception exception) {
//               System.out.println(exception.getMessage());
//           }
//       }
}