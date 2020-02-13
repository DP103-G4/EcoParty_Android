package tw.dp103g4.friend;

public class StateMsg {
	private String type;
	private int userId;
	
	public StateMsg(String type, int userId) {
		super();
		this.type = type;
		this.userId = userId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}
