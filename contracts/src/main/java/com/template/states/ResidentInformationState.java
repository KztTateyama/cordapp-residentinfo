package com.template.states;

import com.template.contracts.ResidentInformationContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.List;

import java.util.Date;


// *********
// * State *
// *********
@BelongsToContract(ResidentInformationContract.class)
public class ResidentInformationState implements ContractState {

    @NonNull    public final String     residentName;		// 居住者名
    @NonNull    public final String     myNumber;			// マイナンバー
    @NonNull    public final Party      currentCity;		// 現自治体
    @NonNull    public final String     currentAddress;		// 現住所
    @NonNull    public final Date       birthday;			// 生年月日
                public final Party      oldCity;			// 旧自治体
                public final String     oldAddress; 		// 旧住所


    @ConstructorForDeserialization
    public ResidentInformationState(String ResidentName,String MyNumber, Party CurrentCity,String CurrentAddress,
                                   Date Birthday, Party OldCity, String OldAddress) {
        this.residentName       = ResidentName;
        this.myNumber           = MyNumber;
        this.currentCity        = CurrentCity;
        this.currentAddress     = CurrentAddress;
        this.birthday           = Birthday;
        this.oldCity            = OldCity;
        this.oldAddress         = OldAddress;
    }

    public Party getOldCity(){ return oldCity; }

    public Party getCurrentCity(){ return currentCity; }

    public String getResidentName(){ return residentName; }

    public String getMyNumber(){ return myNumber; }

    public Date getBirthday(){ return birthday; }

    public String getOldAddress(){ return oldAddress; }

    public String getCurrentAddress(){ return  currentAddress; }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(oldCity,currentCity);
    }

    /* withNewCurrentCity
     * 現在の自治体と現住所を引数の値に置き換える。
     * また、旧自治体に現自治体の値を、旧住所に現住所を設定する。
     */
    public ResidentInformationState withNewCurrentCity(Party newCurrentCity,String newCurrentAddress) {
        return new ResidentInformationState(residentName, myNumber,newCurrentCity, newCurrentAddress,birthday, currentCity, currentAddress);
    }

}