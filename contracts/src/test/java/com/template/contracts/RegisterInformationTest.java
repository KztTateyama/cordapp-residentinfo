package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
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
//            Arrays.asList("net.corda.training", "net.corda.finance.contracts")
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );

    @Test
    public void mustIncludeIssueCommand() {

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
                    tx.output(ResidentInformationContract.IOU_CONTRACT_ID, iou);
                    tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new ResidentInformationContract.Commands.RegisterInformation()); // Correct type.
                    return tx.verifies();
                });
                return null;
            });
        } catch (ParseException e){
            e.printStackTrace();
        }
    }
    
    @Test
    public void dummyTest() {

    }
}