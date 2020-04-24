package com.template;

import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateRef;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;

import com.template.flows.RegisterInformationFlow;
import com.template.flows.ChangeInformationFlow;
import com.template.states.ResidentInformationState;
import com.template.contracts.ResidentInformationContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class ChangeInformationFlowTests {

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
        RegisterInformationFlow.InitiatorFlow flow =
                new RegisterInformationFlow.InitiatorFlow(iouState.residentName,
                        iouState.myNumber,
                        iouState.currentCity,
                        iouState.currentAddress,
                        iouState.birthday);
        CordaFuture future = b.startFlow(flow);
        mockNetwork.runNetwork();
        return (SignedTransaction) future.get();
    }

    /**
     * Task 1.
     * Build out the beginnings of [ChangeInformationFlow]!
     */
    @Test
    public void flowReturnsCorrectlyFormedPartiallySignedTransaction() throws Exception {

        Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
        Party CCity = c.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
        String newAddress = "CCity DDD-EEE";

        ResidentInformationState iou =
                new ResidentInformationState(
                        "Yamada Taro",
                        "123456789012",
                        BCity,
                        "BCity YYY-ZZZ",
                        "1990-02-10",
                        "ACity XXX-YYY"
                );

        SignedTransaction stx = issueIOU(iou);
        ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
        ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),CCity,newAddress);
        Future<SignedTransaction> future = b.startFlow(flow);

        mockNetwork.runNetwork();

        SignedTransaction ptx = future.get();

        // Check the transaction is well formed...
        // One output ResidentInformationState, one input state reference and a Transfer command with the right properties.
        assert (ptx.getTx().getInputs().size() == 1);
        assert (ptx.getTx().getOutputs().size() == 1);
        assert (ptx.getTx().getOutputs().get(0).getData() instanceof ResidentInformationState);
        assert (ptx.getTx().getInputs().get(0).equals(new StateRef(stx.getId(), 0)));

        Command command = ptx.getTx().getCommands().get(0);

        assert (command.getValue().equals(new ResidentInformationContract.Commands.ChangeInformation()));
        ptx.verifySignaturesExcept(b.getInfo().getLegalIdentities().get(0).getOwningKey(), c.getInfo().getLegalIdentities().get(0).getOwningKey(), mockNetwork.getDefaultNotaryIdentity().getOwningKey());

    }

       /**
        * Task 2.
        * Check that an [ResidentInformationState] cannot be transferred to the same currentCity.
        */
       @Test
       public void iouCannotBeTransferredToSameParty() throws Exception {

           Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
           String newAddress = "CCity DDD-EEE";

           ResidentInformationState iou =
                   new ResidentInformationState(
                           "Yamada Taro",
                           "123456789012",
                           BCity,
                           "BCity YYY-ZZZ",
                           "1990-02-10",
                           "ACity XXX-YYY"
                   );

           SignedTransaction stx = issueIOU(iou);
           ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
           ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),BCity,newAddress);
           Future<SignedTransaction> future = b.startFlow(flow);

           try {
               mockNetwork.runNetwork();
               future.get();
           } catch (Exception exception) {
               System.out.println(exception.getMessage());
               assert exception.getMessage().contains("Failed requirement: The currentCity property must change in a transfer.");
           }

   }

       /**
        * Task 3.
        * Get the borrowers and the new lenders signatures.
        * TODO: Amend the [ChangeInformationFlow] to handle collecting signatures from multiple parties.
        * Hint: use [initiateFlow] and the [CollectSignaturesFlow] in the same way you did for the [RegisterInformationFlow].
        */
       @Test
       public void flowReturnsTransactionSignedBtAllParties() throws Exception {

            Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
            Party CCity = c.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
            String newAddress = "CCity DDD-EEE";

            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity,
                            "BCity YYY-ZZZ",
                            "1990-02-10",
                            "ACity XXX-YYY"
                    );

            SignedTransaction stx = issueIOU(iou);
            ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
            ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),CCity,newAddress);
            Future<SignedTransaction> future = b.startFlow(flow);

            try {
                mockNetwork.runNetwork();
                future.get();
                stx.verifySignaturesExcept(mockNetwork.getDefaultNotaryIdentity().getOwningKey());
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }

    }

      /**
       * Task 4.
       * We need to get the transaction signed by the notary service
       * TODO: Use a subFlow call to the [FinalityFlow] to get a signature from the lender.
       */
      @Test
      public void flowReturnsTransactionSignedByAllPartiesAndNotary() throws Exception {

           Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
           Party CCity = c.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
           String newAddress = "CCity DDD-EEE";

           ResidentInformationState iou =
                   new ResidentInformationState(
                           "Yamada Taro",
                           "123456789012",
                           BCity,
                           "BCity YYY-ZZZ",
                           "1990-02-10",
                           "ACity XXX-YYY"
                   );

           SignedTransaction stx = issueIOU(iou);
           ResidentInformationState inputIou = (ResidentInformationState) stx.getTx().getOutputs().get(0).getData();
           ChangeInformationFlow.InitiatorFlow flow = new ChangeInformationFlow.InitiatorFlow(inputIou.getLinearId(),CCity,newAddress);
           Future<SignedTransaction> future = b.startFlow(flow);

           try {
               mockNetwork.runNetwork();
               future.get();
               stx.verifyRequiredSignatures();
           } catch (Exception exception) {
               System.out.println(exception.getMessage());
           }

      }
}