package tw.dp103g4.partydetail;

public class PartyLike {
    private int userId;
    private int partyId;
    public PartyLike(int userId, int partyId) {
        super();
        this.userId = userId;
        this.partyId = partyId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getPartyId() {
        return partyId;
    }
    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

}
