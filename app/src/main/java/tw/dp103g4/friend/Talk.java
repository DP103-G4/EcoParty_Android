package tw.dp103g4.friend;

import java.io.Serializable;
import java.util.Date;

public class Talk implements Serializable {
	private int id;
	private int receiverId;
	private int senderId;
	private int partyId;
	private String content;
	private Date time;
	private Boolean isRead;

	public Talk(int receiverId,int senderId, int partyId, String content, Date time) {
		super();

		this.receiverId = receiverId;
		this.senderId = senderId;
		this.partyId = partyId;
		this.content = content;
		this.time = time;

	}

	public Talk(int receiverId,int senderId, int partyId, String content) {
		super();

		this.receiverId = receiverId;
		this.senderId = senderId;
		this.partyId = partyId;
		this.content = content;

	}

	

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}

	public int getSenderId() {
		return senderId;
	}
	
	

	public void setSenderId(int senderId) {
		this.senderId = senderId;
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

	public Boolean getIsRead() {
		return isRead;
	}

	public void setIsRead(Boolean read) {
		this.isRead = read;
	}

}

