package tw.dp103g4.location;

import com.google.android.gms.maps.model.LatLng;

public class InfoLocation {
	private int id;
	private int partyId;
	private int userId;
	private double latitude;
	private double longitude;
	private String name;
	private String content;

	public InfoLocation(String name, String content) {
		this.name = name;
		this.content = content;
	}

	public InfoLocation(int id, int partyId, int userId, double latitude, double longitude, String name, String content) {
		super();
		this.id = id;
		this.partyId = partyId;
		this.userId = userId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.content = content;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}

	public InfoLocation(int id) {
		super();
		this.id = id;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPartyId() {
		return partyId;
	}

	public void setPartyId(int partyId) {
		this.partyId = partyId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	
}
