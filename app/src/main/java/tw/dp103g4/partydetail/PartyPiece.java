package tw.dp103g4.partydetail;

import java.util.Date;

public class PartyPiece {
	private int id;
	private int userId;
	private int partyId;
	private String content;
	private Date time;

	public PartyPiece(int userId, int partyId, String content) {
		super();
		this.userId = userId;
		this.partyId = partyId;
		this.content = content;
	}
	
	public PartyPiece(int id, int userId, int partyId, String content, Date time) {
		super();
		this.id = id;
		this.userId = userId;
		this.partyId = partyId;
		this.content = content;
		this.time = time;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
