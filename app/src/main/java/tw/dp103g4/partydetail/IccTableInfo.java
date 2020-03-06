package tw.dp103g4.partydetail;

public class IccTableInfo {
	private IccTable iccTable;
	private String userName;
	private int count;
	
	public IccTableInfo(IccTable iccTable, String userName, int count) {
		super();
		this.iccTable = iccTable;
		this.userName = userName;
		this.count = count;
	}
	public IccTable getIccTable() {
		return iccTable;
	}
	public void setIccTable(IccTable iccTable) {
		this.iccTable = iccTable;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	
}
