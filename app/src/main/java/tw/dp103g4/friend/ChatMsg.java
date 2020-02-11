package tw.dp103g4.friend;

public class ChatMsg {
	private String type;
	private int sender;
	private int receiver;
	private String message;

	public ChatMsg(String type, int sender, int receiver, String message) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}

	public int getSender() {
		return sender;
	}

	public void setSender(int sender) {
		this.sender = sender;
	}

	public int getReceiver() {
		return receiver;
	}

	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
