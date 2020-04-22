package com.template.states;

import com.template.contracts.ResidentInformationContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import java.util.Date;


// *********
// * State *
// *********
@BelongsToContract(ResidentInformationContract.class)
public class ResidentInformationState implements ContractState,LinearState  {

    public final String     residentName;
    public final String     myNumber;
    public final Party      currentCity;
    public final String     currentAddress;
    public final Date       birthday;
    public final Party      oldCity;
    public final String     oldAddress;

    private final UniqueIdentifier linearId;


    @ConstructorForDeserialization
    private ResidentInformationState(String residentName,String myNumber, Party currentCity,String currentAddress,
                                   Date birthday, Party oldCity, String oldAddress,UniqueIdentifier linearId) {
        this.residentName       = residentName;
        this.myNumber           = myNumber;
        this.currentCity        = currentCity;
        this.currentAddress     = currentAddress;
        this.birthday           = birthday;
        this.oldCity            = oldCity;
        this.oldAddress         = oldAddress;
        this.linearId           = linearId;
    }

    public ResidentInformationState(String residentName,String myNumber, Party currentCity,String currentAddress,
                                    Date birthday, Party oldCity, String oldAddress) {
        this(residentName, myNumber, currentCity, currentAddress, birthday, oldCity, oldAddress,new UniqueIdentifier());

    }

    public Party getOldCity(){ return oldCity; }

    public Party getCurrentCity(){ return currentCity; }

    public String getResidentName(){ return residentName; }

    public String getMyNumber(){ return myNumber; }

    public Date getBirthday(){ return birthday; }

    public String getOldAddress(){ return oldAddress; }

    public String getCurrentAddress(){ return  currentAddress; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(oldCity,currentCity);
    }

    /* withNewCurrentCity
     * change currentcity and Address.
     * and change oldcity and oldaddress set currentcity and address.
     */
    public ResidentInformationState withNewCurrentCity(Party newCurrentCity,String newCurrentAddress) {
        return new ResidentInformationState(residentName, myNumber,newCurrentCity, newCurrentAddress,birthday, currentCity, currentAddress);
    }

}