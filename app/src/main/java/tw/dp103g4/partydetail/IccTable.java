package tw.dp103g4.partydetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IccTable {
	private int userId;
	private int partyId;
	private double weight;
	private int plastic01;
	private int plastic02;
	private int plastic03;
	private int plastic04;
	private int plasticBag01;
	private int plasticBag02;
	private int washless01;
	private int washless02;
	private int washless03;
	private int others01;
	private int others02;
	private int others03;
	private int fishery01;
	private int fishery02;
	private int fishery03;
	private int personal01;
	private int personal02;
	private int smoke01;
	private int smoke02;
	private int care01;
	private int care02;
	private int care03;
	private int care04;
	private String care01Name = "當地關心的廢棄物1";
	private String care02Name = "當地關心的廢棄物2";
	private String care03Name = "當地關心的廢棄物3";
	private String care04Name = "當地關心的廢棄物4";
	
	
	public IccTable(int userId, int partyId) {
		super();
		this.userId = userId;
		this.partyId = partyId;
	}
	
	public IccTable(int userId, int partyId, double weight, int plastic01, int plastic02, int plastic03, int plastic04,
			int plasticBag01, int plasticBag02, int washless01, int washless02, int washless03, int others01,
			int others02, int others03, int fishery01, int fishery02, int fishery03, int personal01, int personal02,
			int smoke01, int smoke02, int care01, int care02, int care03, int care04, String care01Name,
			String care02Name, String care03Name, String care04Name) {
		super();
		this.userId = userId;
		this.partyId = partyId;
		this.weight = weight;
		this.plastic01 = plastic01;
		this.plastic02 = plastic02;
		this.plastic03 = plastic03;
		this.plastic04 = plastic04;
		this.plasticBag01 = plasticBag01;
		this.plasticBag02 = plasticBag02;
		this.washless01 = washless01;
		this.washless02 = washless02;
		this.washless03 = washless03;
		this.others01 = others01;
		this.others02 = others02;
		this.others03 = others03;
		this.fishery01 = fishery01;
		this.fishery02 = fishery02;
		this.fishery03 = fishery03;
		this.personal01 = personal01;
		this.personal02 = personal02;
		this.smoke01 = smoke01;
		this.smoke02 = smoke02;
		this.care01 = care01;
		this.care02 = care02;
		this.care03 = care03;
		this.care04 = care04;
		this.care01Name = care01Name;
		this.care02Name = care02Name;
		this.care03Name = care03Name;
		this.care04Name = care04Name;
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
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public int getPlastic01() {
		return plastic01;
	}
	public void setPlastic01(int plastic01) {
		this.plastic01 = plastic01;
	}
	public int getPlastic02() {
		return plastic02;
	}
	public void setPlastic02(int plastic02) {
		this.plastic02 = plastic02;
	}
	public int getPlastic03() {
		return plastic03;
	}
	public void setPlastic03(int plastic03) {
		this.plastic03 = plastic03;
	}
	public int getPlastic04() {
		return plastic04;
	}
	public void setPlastic04(int plastic04) {
		this.plastic04 = plastic04;
	}
	public int getPlasticBag01() {
		return plasticBag01;
	}
	public void setPlasticBag01(int plasticBag01) {
		this.plasticBag01 = plasticBag01;
	}
	public int getPlasticBag02() {
		return plasticBag02;
	}
	public void setPlasticBag02(int plasticBag02) {
		this.plasticBag02 = plasticBag02;
	}
	public int getWashless01() {
		return washless01;
	}
	public void setWashless01(int washless01) {
		this.washless01 = washless01;
	}
	public int getWashless02() {
		return washless02;
	}
	public void setWashless02(int washless02) {
		this.washless02 = washless02;
	}
	public int getWashless03() {
		return washless03;
	}
	public void setWashless03(int washless03) {
		this.washless03 = washless03;
	}
	public int getOthers01() {
		return others01;
	}
	public void setOthers01(int others01) {
		this.others01 = others01;
	}
	public int getOthers02() {
		return others02;
	}
	public void setOthers02(int others02) {
		this.others02 = others02;
	}
	public int getOthers03() {
		return others03;
	}
	public void setOthers03(int others03) {
		this.others03 = others03;
	}
	public int getFishery01() {
		return fishery01;
	}
	public void setFishery01(int fishery01) {
		this.fishery01 = fishery01;
	}
	public int getFishery02() {
		return fishery02;
	}
	public void setFishery02(int fishery02) {
		this.fishery02 = fishery02;
	}
	public int getFishery03() {
		return fishery03;
	}
	public void setFishery03(int fishery03) {
		this.fishery03 = fishery03;
	}
	public int getPersonal01() {
		return personal01;
	}
	public void setPersonal01(int personal01) {
		this.personal01 = personal01;
	}
	public int getPersonal02() {
		return personal02;
	}
	public void setPersonal02(int personal02) {
		this.personal02 = personal02;
	}
	public int getSmoke01() {
		return smoke01;
	}
	public void setSmoke01(int smoke01) {
		this.smoke01 = smoke01;
	}
	public int getSmoke02() {
		return smoke02;
	}
	public void setSmoke02(int smoke02) {
		this.smoke02 = smoke02;
	}
	public int getCare01() {
		return care01;
	}
	public void setCare01(int care01) {
		this.care01 = care01;
	}
	public int getCare02() {
		return care02;
	}
	public void setCare02(int care02) {
		this.care02 = care02;
	}
	public int getCare03() {
		return care03;
	}
	public void setCare03(int care03) {
		this.care03 = care03;
	}
	public int getCare04() {
		return care04;
	}
	public void setCare04(int care04) {
		this.care04 = care04;
	}
	public String getCare01Name() {
		return care01Name;
	}
	public void setCare01Name(String care01Name) {
		this.care01Name = care01Name;
	}
	public String getCare02Name() {
		return care02Name;
	}
	public void setCare02Name(String care02Name) {
		this.care02Name = care02Name;
	}
	public String getCare03Name() {
		return care03Name;
	}
	public void setCare03Name(String care03Name) {
		this.care03Name = care03Name;
	}
	public String getCare04Name() {
		return care04Name;
	}
	public void setCare04Name(String care04Name) {
		this.care04Name = care04Name;
	}
	
	public void setIcc(int index, int count) {
		switch (index) {
		case 0:
			this.plastic01 = count;
			break;
		case 1:
			this.plastic02 = count;
			break;
		case 2:
			this.plastic03 = count;
			break;
		case 3:
			this.plastic04 = count;
			break;
		case 4:
			this.plasticBag01 = count;
			break;
		case 5:
			this.plasticBag02 = count;
			break;
		case 6:
			this.washless01 = count;
			break;
		case 7:
			this.washless02 = count;
			break;
		case 8:
			this.washless03 = count;
			break;
		case 9:
			this.others01 = count;
			break;
		case 10:
			this.others02 = count;
			break;
		case 11:
			this.others03 = count;
			break;
		case 12:
			this.fishery01 = count;
			break;
		case 13:
			this.fishery02 = count;
			break;
		case 14:
			this.fishery03 = count;
			break;
		case 15:
			this.personal01 = count;
			break;
		case 16:
			this.personal02 = count;
			break;
		case 17:
			this.smoke01 = count;
			break;
		case 18:
			this.smoke02 = count;
			break;
		case 19:
			this.care01 = count;
			break;
		case 20:
			this.care02 = count;
			break;
		case 21:
			this.care03 = count;
			break;
		case 22:
			this.care04 = count;
			break;
		}
	}
	
	public void setIcc(int index, Double weight) {
		switch (index) {
		case 23:
			this.weight = weight;
			break;
		}
	}
	
	public void setIccName(int index, String name) {
		switch (index) {
		case 0:
			this.care01Name = name;
			break;
		case 1:
			this.care02Name = name;
			break;
		case 2:
			this.care03Name = name;
			break;
		case 3:
			this.care04Name = name;
			break;
		}
	}
	
	public Object getIcc(int index) {
		switch (index) {
		case 0:
			return this.plastic01;
		case 1:
			return this.plastic02;
		case 2:
			return this.plastic03;
		case 3:
			return this.plastic04;
		case 4:
			return this.plasticBag01;
		case 5:
			return this.plasticBag02;
		case 6:
			return this.washless01;
		case 7:
			return this.washless02;
		case 8:
			return this.washless03;
		case 9:
			return this.others01;
		case 10:
			return this.others02;
		case 11:
			return this.others03;
		case 12:
			return this.fishery01;
		case 13:
			return this.fishery02;
		case 14:
			return this.fishery03;
		case 15:
			return this.personal01;
		case 16:
			return this.personal02;
		case 17:
			return this.smoke01;
		case 18:
			return this.smoke02;
		case 19:
			return this.care01;
		case 20:
			return this.care02;
		case 21:
			return this.care03;
		case 22:
			return this.care04;
		case 23:
			return this.weight;
		}
		return null;
	}
	
	public String getIccName(int index) {
		List<String> nameList = new ArrayList<String>(
				Arrays.asList("寶特瓶", "塑膠瓶蓋", "食物瓶罐", "非食物瓶罐", "塑膠提袋", "食品包裝袋",
						"吸管", "外帶飲料杯", "免洗餐具", "鐵鋁罐", "鋁箔包", "玻璃瓶", "釣魚用具",
						"漁業浮球", "漁網與繩子", "牙刷", "針筒與針頭", "菸蒂", "打火機"));
		nameList.add(this.care01Name);
		nameList.add(this.care02Name);
		nameList.add(this.care03Name);
		nameList.add(this.care04Name);
		nameList.add("總重量");

		if (index >= 0 && index <= 23) {
			return nameList.get(index);
		}
		
		return "no name";
	}
	
}
