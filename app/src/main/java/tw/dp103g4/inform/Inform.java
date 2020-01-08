package tw.dp103g4.inform;

import java.util.Date;

public class Inform {
	private int id;
	private int userId;
	private int partyId;
	private Date time;
	private String content;
	private boolean isRead;
	
	public Inform(int id, int userId, int partyId, Date time, String content, boolean isRead) {
		super();
		this.id = id;
		this.userId = userId;
		this.partyId = partyId;
		this.time = time;
		this.content = content;
		this.isRead = isRead;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isRead() {
		return isRead;
	}

	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	
	
}
