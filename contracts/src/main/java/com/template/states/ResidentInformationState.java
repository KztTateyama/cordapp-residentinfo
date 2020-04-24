package com.template.states;

import com.template.contracts.ResidentInformationContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ResidentInformationContract.class)
public class ResidentInformationState implements ContractState,LinearState  {

    public final String     residentName;
    public final String     myNumber;
    public final Party      currentCity;
    public final String     currentAddress;
    public final String       birthday;
    public final String     oldAddress;
    private final UniqueIdentifier linearId;


    @ConstructorForDeserialization
    private ResidentInformationState(String residentName,String myNumber, Party currentCity,String currentAddress,
                                   String birthday, String oldAddress,UniqueIdentifier linearId) {
        this.residentName       = residentName;
        this.myNumber           = myNumber;
        this.currentCity        = currentCity;
        this.currentAddress     = currentAddress;
        this.birthday           = birthday;
        this.oldAddress         = oldAddress;
        this.linearId           = linearId;
    }

    public ResidentInformationState(String residentName,String myNumber, Party currentCity,String currentAddress,
                                    String birthday,String oldAddress) {
        this(residentName, myNumber, currentCity, currentAddress, birthday, oldAddress,new UniqueIdentifier());

    }

    // field's accessor
    public String getResidentName(){ return residentName; }

    public String getMyNumber(){ return myNumber; }

    public Party getCurrentCity(){ return currentCity; }

    public String getCurrentAddress(){ return  currentAddress; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(currentCity);
    }

    /* withNewCurrentCity
     * change currentcity and Address.
     */
    public ResidentInformationState withNewCurrentCity(Party newCurrentCity,String newCurrentAddress) {
        return new ResidentInformationState(residentName, myNumber,newCurrentCity, newCurrentAddress,birthday, currentAddress);
    }

}