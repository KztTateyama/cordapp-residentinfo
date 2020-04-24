package com.template.contracts;

import static com.template.TestUtils.*;
import com.template.states.ResidentInformationState;
import static net.corda.testing.node.NodeTestUtils.ledger;
import net.corda.testing.node.MockServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import org.junit.Test;


public class ChangeInformationTest {

    private final SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );

    @Test
    public void mustHandleMultipleCommandValues() {

        ResidentInformationState iou =
            new ResidentInformationState(
                "Yamada Taro",
                "123456789012",
                BCity.getParty(),
                "BCity YYY-ZZZ",
                "1990-02-10",
               "ACity XXX-YYY"
             );

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }


    @Test
    public void mustHaveOneInputAndOneOutput() {

        ResidentInformationState iou =
                new ResidentInformationState(
                        "Yamada Taro",
                        "123456789012",
                        BCity.getParty(),
                        "BCity YYY-ZZZ",
                        "1990-02-10",
                        "ACity XXX-YYY"
                );

        ResidentInformationState dummyState =
                new ResidentInformationState(
                        "Dummy Taro",
                        "999999999999",
                        XCity.getParty(),
                        "XCity YYY-ZZZ",
                        "1990-02-10",
                        "YCity XXX-YYY"
                );

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, dummyState);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, dummyState);
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }


    @Test
    public void theLenderMustChange() {

        ResidentInformationState iou =
                new ResidentInformationState(
                        "Yamada Taro",
                        "123456789012",
                        BCity.getParty(),
                        "BCity YYY-ZZZ",
                        "1990-02-10",
                        "ACity XXX-YYY"
                );

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation());  // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

    @Test
    public void allParticipantsMustSign() {

        ResidentInformationState iou =
                new ResidentInformationState(
                        "Yamada Taro",
                        "123456789012",
                        BCity.getParty(),
                        "BCity YYY-ZZZ",
                        "1990-02-10",
                        "ACity XXX-YYY"
                );


        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                tx.command(Arrays.asList(BCity.getPublicKey(),XCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

}