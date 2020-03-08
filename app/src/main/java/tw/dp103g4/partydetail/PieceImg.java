package tw.dp103g4.partydetail;

public class PieceImg {
	private int id;
	private int pieceId;
	public PieceImg(int id, int pieceId) {
		super();
		this.id = id;
		this.pieceId = pieceId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPieceId() {
		return pieceId;
	}
	public void setPieceId(int pieceId) {
		this.pieceId = pieceId;
	}
}
