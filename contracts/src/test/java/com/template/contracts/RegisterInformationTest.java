package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import com.template.states.ResidentInformationState;
import static com.template.TestUtils.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class RegisterInformationTest {

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
     * Make sure the "RegisterInformation" command is valid.
     */
    @Test
    public void mustIncludeResisterCommand() {

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
                    tx.command(BCity.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
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
     * Make sure States only sets output.
     * If it doesn't include output,or the state contains input, throw an error.
     */
    @Test
    public void issueTransactionMustHaveNoInputs() {

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
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, new DummyState());
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.input(ResidentInformationContract.IOU_CONTRACT_ID, new DummyState());
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
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
     * Make sure that only one output is allowed in one Transaction.
     */
    @Test
    public void issueTransactionMustHaveOneOutput() {

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
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou); // Two outputs fails.
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
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
     * Make sure to return an error if the residentName is null.
     */
    @Test
    public void cannotCreateNullresidentName() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "",
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
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 5.
     * Make sure to return an error if the myNumber is null.
     */
    @Test
    public void cannotCreateNullmyNumber() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            ledger(ledgerServices, l -> {
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 5.
     * Make sure to return an error if the currentAddress is null.
     */
    @Test
    public void cannotCreateNullcurrentAddress() {

        try{
            ResidentInformationState iou =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            ledger(ledgerServices, l -> {
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

    /**
     * Task 6.
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
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(XCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(ACity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.fails();
                });
                l.transaction(tx -> {
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(BCity.getPublicKey(), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }

}