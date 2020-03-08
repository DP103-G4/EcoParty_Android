package tw.dp103g4.partydetail;

import java.sql.Date;

public class MsgWarn {
	private int id;
	private int messageId;
	private int userId;
	private Date time;
	private String content;

	public MsgWarn(int messageId, int userId, String content) {
		super();
		this.messageId = messageId;
		this.userId = userId;
		this.content = content;
	}
	
	
	public MsgWarn(int id,int messageId,int userId,Date time,String content) {
		super();
		this.id = id;
		this.messageId = messageId;
		this.userId = userId;
		this.time = time;
		this.content = content;
		
		}
	
	@Override
	public String toString() {
		String text = "ID"+id+"\n content"+ content;
		return text;
	}

	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getMessageId() {
		return messageId;
	}
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
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
	
	
}
