package tw.dp103g4.partydetail;

public class PieceInfo {
	private PartyPiece partyPiece;
	private String ownerName;
	public PieceInfo(PartyPiece partyPiece, String ownerName) {
		super();
		this.partyPiece = partyPiece;
		this.ownerName = ownerName;
	}
	public PartyPiece getPartyPiece() {
		return partyPiece;
	}
	public void setPartyPiece(PartyPiece partyPiece) {
		this.partyPiece = partyPiece;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
}
