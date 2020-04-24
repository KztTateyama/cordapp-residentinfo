package com.template;

import net.corda.core.contracts.Command;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;

import com.template.flows.RegisterInformationFlow;
import com.template.states.ResidentInformationState;
import com.template.contracts.ResidentInformationContract;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class RegisterInformationFlowTests {

    private MockNetwork mockNetwork;
    private StartedMockNode a, b;

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

        ArrayList<StartedMockNode> startedNodes = new ArrayList<>();
        startedNodes.add(a);
        startedNodes.add(b);

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Task 1.
     * Build Transaction from the flow, sign it, and confirm that State is generated.
     */
    @Test
    public void flowReturnsCorrectlyFormedPartiallySignedTransaction() throws Exception {

        Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
        Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

        ResidentInformationState iou =
                new ResidentInformationState(
                        "Yamada Taro",
                        "123456789012",
                        BCity,
                        "BCity YYY-ZZZ",
                        "1990-02-10",
                        "ACity XXX-YYY"
                );

        //RegisterInformationFlow.InitiatorFlow flow = new RegisterInformationFlow.InitiatorFlow(iou);
        RegisterInformationFlow.InitiatorFlow flow =
                new RegisterInformationFlow.InitiatorFlow(iou.residentName,
                        iou.myNumber,
                        iou.currentCity,
                        iou.currentAddress,
                        iou.birthday);

        Future<SignedTransaction> future = b.startFlow(flow);
        mockNetwork.runNetwork();

        // Return the unsigned(!) SignedTransaction object from the RegisterInformationFlow.
        SignedTransaction ptx = future.get();

        // Print the transaction for debugging purposes.
        System.out.println(ptx.getTx());

        // Check the transaction is well formed...
        // No inputs, one output ResidentInformationState and a command with the right properties.
        assert (ptx.getTx().getInputs().isEmpty());
        assert (ptx.getTx().getOutputs().get(0).getData() instanceof ResidentInformationState);

        // Check that command is RegisterInformation.
        Command command = ptx.getTx().getCommands().get(0);
        assert (command.getValue() instanceof ResidentInformationContract.Commands.RegisterInformation);

        // Check that All expected signers signed.
        final List<PublicKey> requiredSigners = command.getSigners();
        List<PublicKey> expectedSigners = new ArrayList<>();
        expectedSigners.add(iou.getCurrentCity().getOwningKey());

        assert (requiredSigners.containsAll(expectedSigners));

    }

}
