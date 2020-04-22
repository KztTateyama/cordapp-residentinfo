package com.template.contracts;

import com.template.states.ResidentInformationState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static com.template.TestUtils.*;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class DeleteInformationTest {

    private final SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

    // A pre-defined dummy command.
    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands{}
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );

    /**
     * Task 1.
     * Make sure the "DeleteInformation" command is valid.
     */
    @Test
    public void mustIncludeDeleteCommand() {

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
                    tx.command(BCity.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 2.
     * Make sure States only sets input.
     * If the state contains output, throw an error.
     */
    @Test
    public void issueTransactionMustHaveNoOutputs() {

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
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, new DummyState());
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, new DummyState());
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 3.
     * Make sure that only one input is allowed in one Transaction.
     */
    @Test
    public void issueTransactionMustHaveOneInput() {

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
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou); // Two outputs fails.
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 4.
     * Make sure that only the signature of currentCity is valid.
     */
    @Test
    public void signerCheckOnlyCurrentCity() {

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
//                    tx.command(Arrays.asList(XCity.getPublicKey()), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    tx.command(XCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(ACity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.DeleteInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

}