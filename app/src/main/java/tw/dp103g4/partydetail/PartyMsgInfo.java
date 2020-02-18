package tw.dp103g4.partydetail;

public class PartyMsgInfo {
	private PartyMessage partyMessage;
	private String msgName;
	public PartyMsgInfo(PartyMessage partyMessage, String msgName) {
		super();
		this.partyMessage = partyMessage;
		this.msgName = msgName;
	}
	public PartyMessage getPartyMessage() {
		return partyMessage;
	}
	public void setPartyMessage(PartyMessage partyMessage) {
		this.partyMessage = partyMessage;
	}
	public String getMsgName() {
		return msgName;
	}
	public void setMsgName(String msgName) {
		this.msgName = msgName;
	}
	
	
	

}
