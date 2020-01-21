package tw.dp103g4.partydetail;

public class Participant {
	private int id;
	private int partyId;
	private int count;
	private boolean isArrival;
	private boolean isStaff;
	
	
	
	public Participant(int id, int partyId, int count, boolean isArrival, boolean isStaff) {
		super();
		this.id = id;
		this.partyId = partyId;
		this.count = count;
		this.isArrival = isArrival;
		this.isStaff = isStaff;
	}

	public Participant(int id, int partyId, int count) {
		super();
		this.id = id;
		this.partyId = partyId;
		this.count = count;
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
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean isArrival() {
		return isArrival;
	}
	public void setArrival(boolean isArrival) {
		this.isArrival = isArrival;
	}
	public boolean isStaff() {
		return isStaff;
	}
	public void setStaff(boolean isStaff) {
		this.isStaff = isStaff;
	}
	
	

	

}
