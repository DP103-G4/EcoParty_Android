package tw.dp103g4.partydetail;

import tw.dp103g4.partylist_android.Party;

public class PartyInfo {
    private Party party;
    private Boolean isIn;
    private Boolean	isLike;
    private Boolean isStaff;

    public PartyInfo(Party party, Boolean isIn, Boolean isLike, Boolean isStaff) {
        super();
        this.party = party;
        this.isIn = isIn;
        this.isLike = isLike;
        this.isStaff = isStaff;
    }
    public Party getParty() {
        return party;
    }
    public void setParty(Party party) {
        this.party = party;
    }
    public Boolean getIsIn() {
        return isIn;
    }
    public void setIsIn(Boolean isIn) {
        this.isIn = isIn;
    }
    public Boolean getIsLike() {
        return isLike;
    }
    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }
    public Boolean getIsStaff() {
        return isStaff;
    }
    public void setIsStaff(Boolean isStaff) {
        this.isStaff = isStaff;
    }

}
