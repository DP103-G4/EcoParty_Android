package tw.dp103g4.partydetail;

public class ParticipantInfo {
	private Participant participant;
	private String participantName;
	
	public ParticipantInfo(Participant participant, String participantName) {
		super();
		this.participant = participant;
		this.participantName = participantName;
	}
	public Participant getParticipant() {
		return participant;
	}
	public void setParticipant(Participant participant) {
		this.participant = participant;
	}
	public String getParticipantName() {
		return participantName;
	}
	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	
}
