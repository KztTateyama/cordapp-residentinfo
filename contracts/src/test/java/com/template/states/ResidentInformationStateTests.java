package com.template.states;

import net.corda.core.identity.Party;
import org.junit.Test;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;
import static com.template.TestUtils.*;

/**
 * Practical exercise instructions.
 * Uncomment the first unit test [hasIOUAmountFieldOfCorrectType()] then run the unit test using the green arrow
 * to the left of the {@link IOUStateTests} class or the [hasIOUAmountFieldOfCorrectType()] method.
 * Running the unit tests from {@link IOUStateTests} runs all of the unit tests defined in the class.
 * The test should fail because you need to make some changes to the IOUState to make the test pass. Read the TODO
 * under each task number for a description and a hint of what you need to do.
 * Once you have the unit test passing, uncomment the next test.
 * Continue until all the unit tests pass.
 * Hint: CMD / Ctrl + click on the brown type names in square brackets for that type's definition in the codebase.
 */

public class ResidentInformationStateTests {

    private final SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");

    /* TODO: Add an 'residentName' property of type {@link String} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasResidentNameFieldOfCorrectType() throws NoSuchFieldException {
        // Does the residentName field exist?
        Field residentNameField = ResidentInformationState.class.getDeclaredField("residentName");
        // Is the residentName field of the correct type?
        assertTrue(residentNameField.getType().isAssignableFrom(String.class));
    }

    /* TODO: Add an 'myNumber' property of type {@link String} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasMyNumberFieldOfCorrectType() throws NoSuchFieldException {
        // Does the myNumber field exist?
        Field myNumberField = ResidentInformationState.class.getDeclaredField("myNumber");
        // Is the myNumber field of the correct type?
        assertTrue(myNumberField.getType().isAssignableFrom(String.class));
    }

    /* TODO: Add an 'currentCity' property of type {@link Party} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasCurrentCityFieldOfCorrectType() throws NoSuchFieldException {
        // Does the currentCity field exist?
        Field currentCityField = ResidentInformationState.class.getDeclaredField("currentCity");
        // Is the currentCity field of the correct type?
        assertTrue(currentCityField.getType().isAssignableFrom(Party.class));
    }

    /* TODO: Add an 'currentAddress' property of type {@link String} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasCurrentAddressFieldOfCorrectType() throws NoSuchFieldException {
        // Does the currentAddress field exist?
        Field currentAddressField = ResidentInformationState.class.getDeclaredField("currentAddress");
        // Is the currentAddress field of the correct type?
        assertTrue(currentAddressField.getType().isAssignableFrom(String.class));
    }


    /* TODO: Add an 'birthday' property of type {@link Date} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasBirthdayFieldOfCorrectType() throws NoSuchFieldException {
        // Does the birthday field exist?
        Field birthdayField = ResidentInformationState.class.getDeclaredField("birthday");
        // Is the birthday field of the correct type?
        assertTrue(birthdayField.getType().isAssignableFrom(Date.class));
    }

    /* TODO: Add an 'oldCity' property of type {@link Party} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasOldCityFieldOfCorrectType() throws NoSuchFieldException {
        // Does the oldCity field exist?
        Field oldCityField = ResidentInformationState.class.getDeclaredField("oldCity");
        // Is the oldCity field of the correct type?
        assertTrue(oldCityField.getType().isAssignableFrom(Party.class));
    }

    /* TODO: Add an 'oldAddress' property of type {@link String} to the {@link ResidentInformationState} class to get this test to pass. */
    @Test
    public void hasOldAddressFieldOfCorrectType() throws NoSuchFieldException {
        // Does the oldAddress field exist?
        Field oldAddressField = ResidentInformationState.class.getDeclaredField("oldAddress");
        // Is the oldAddress field of the correct type?
        assertTrue(oldAddressField.getType().isAssignableFrom(String.class));
    }

    /**
     * Task 5.
     * TODO: Include the oldCity within the {@link ResidentInformationState#getParticipants()} list
     * Hint: [Arrays.asList()] takes any number of parameters and will add them to the list
     */
    @Test
    public void oldCityIsParticipant() {

        try {
            ResidentInformationState residentA =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            assertNotEquals(residentA.getParticipants().indexOf(ACity.getParty()), -1);

        } catch (ParseException e){
            e.printStackTrace();
        }

    }

    /**
     * Task 6.
     * TODO: Include the currentCity within the {@link ResidentInformationState#getParticipants()} list
     * Hint: [Arrays.asList()] takes any number of parameters and will add them to the list
     */
    @Test
    public void currentCityIsParticipant() {
        try {
            ResidentInformationState residentA =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            assertNotEquals(residentA.getParticipants().indexOf(BCity.getParty()), -1);

        } catch (ParseException e){
            e.printStackTrace();
        }

    }

    /**
     * Task 9.
     * TODO: Ensure parameters are ordered correctly.
     * Hint: Make sure that the lender and borrower fields are not in the wrong order as this may cause some
     * confusion in subsequent tasks!
     */
    @Test
    public void checkResidentInformationStateParameterOrdering() throws NoSuchFieldException {

        List<Field> fields = Arrays.asList(ResidentInformationState.class.getDeclaredFields());

        int residentNameIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("residentName"));
        int myNumberIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("myNumber"));
        int currentCityIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("currentCity"));
        int currentAddressIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("currentAddress"));
        int birthdayIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("birthday"));
        int oldCityIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("oldCity"));
        int oldAddressIdx = fields.indexOf(ResidentInformationState.class.getDeclaredField("oldAddress"));

        assertTrue(residentNameIdx < myNumberIdx);
        assertTrue(myNumberIdx < currentCityIdx);
        assertTrue(currentCityIdx < currentAddressIdx);
        assertTrue(currentAddressIdx < birthdayIdx);
        assertTrue(birthdayIdx < oldCityIdx);
        assertTrue(oldCityIdx < oldAddressIdx);
    }

    /**
     * Task 11.
     * TODO: Add a helper method called [withNewCurrentCity] that can be called from an {@link }ResidentInformationState} to change the IOU's lender.
     * - This will also utilize the copy constructor.
     */
    @Test
    public void checkwithNewCurrentCityHelperMethod() {
        try {
            ResidentInformationState residentA =
                    new ResidentInformationState(
                            "Yamada Taro",
                            "123456789012",
                            BCity.getParty(),
                            "BCity YYY-ZZZ",
                            sdformat.parse("1990-02-10"),
                            ACity.getParty(),
                            "ACity XXX-YYY"
                    );

            assertEquals(XCity.getParty(), residentA.withNewCurrentCity(XCity.getParty(),"XCity aaa-bbb").getCurrentCity());
            assertEquals("XCity aaa-bbb", residentA.withNewCurrentCity(XCity.getParty(),"XCity aaa-bbb").getCurrentAddress());

        } catch (ParseException e){
            e.printStackTrace();
        }
    }

}
