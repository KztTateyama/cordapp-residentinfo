package com.template.contracts;

import com.template.states.ResidentInformationState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static com.template.TestUtils.*;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class ChangeInformationTest {

    private final SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

    // A pre-defined dummy command.
    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands{}
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );

    // A dummy state

    @Test
    public void mustHandleMultipleCommandValues() {

        try{
            ResidentInformationState iou =
                new ResidentInformationState(
                    "Yamada Taro",
                    "123456789012",
                    BCity.getParty(),
                    "BCity YYY-ZZZ",
                    sdformat.parse("1990-02-10"),
                    ACity.getParty(),
                   "ACity XXX-YYY"
                 );

            ledger(ledgerServices, l -> {
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new Commands.DummyCommand()); // Wrong type.
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
        } catch (ParseException e){
            e.printStackTrace();
        }
    }


    @Test
    public void mustHaveOneInputAndOneOutput() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            ResidentInformationState dummyState =
                    new ResidentInformationState(
                            "Dummy Taro",
                            "999999999999",
                            XCity.getParty(),
                            "XCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            YCity.getParty(),
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
                    tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou.withNewCurrentCity(XCity.getParty(),"XCity 000-111"));
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, dummyState);
                    tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
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
        } catch (ParseException e){
            e.printStackTrace();
        }
    }


    @Test
    public void theLenderMustChange() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            ledger(ledgerServices, l -> {
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation());
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
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    @Test
    public void allParticipantsMustSign() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
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
                    tx.command(Arrays.asList(XCity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.ChangeInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

}