package tw.dp103g4.partydetail;


public class PartyImg {
	private int id;
	private int partyId;

	public PartyImg(int id, int partyId) {
		super();
		this.id = id;
		this.partyId = partyId;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPartyId() {
		return partyId;
	}
	public void setPartyId(int partyId) {
		this.partyId = partyId;
	}

}
