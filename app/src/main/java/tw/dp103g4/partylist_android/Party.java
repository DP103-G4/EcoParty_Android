package tw.dp103g4.partylist_android;

import java.io.Serializable;
import java.sql.Date;

public class Party implements Serializable {
    private int id;
    private int ownerId;
    private String name;
    private Date startTime;
    private Date endTime;
    private Date postTime;
    private Date endPostTime;
    private String location;
    private String address;
    private double longitude;
    private double latitude;
    private String content;
    private int countUpperLimit;
    private int countLowerLimit;
    private int countCurrent;
    private int state;
    private double distance;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public Date getEndPostTime() {
        return endPostTime;
    }

    public void setEndPostTime(Date endPostTime) {
        this.endPostTime = endPostTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCountUpperLimit() {
        return countUpperLimit;
    }

    public void setCountUpperLimit(int countUpperLimit) {
        this.countUpperLimit = countUpperLimit;
    }

    public int getCountLowerLimit() {
        return countLowerLimit;
    }

    public void setCountLowerLimit(int countLowerLimit) {
        this.countLowerLimit = countLowerLimit;
    }

    public int getCountCurrent() {
        return countCurrent;
    }

    public void setCountCurrent(int countCurrent) {
        this.countCurrent = countCurrent;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public boolean equals(Object obj) {
//        DB移除的id list也有符合這個物件的id 就把這個物件移除掉
        return this.id == ((Party) obj).id;
    }

}
