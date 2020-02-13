package tw.dp103g4.friend;

import java.io.Serializable;
import java.util.Date;

public class NewestTalk implements Serializable {
    private int senderId;
    private String content;
    private Date newMsgTime;
    private String account;

    public NewestTalk(int senderId, String content, Date newMsgTime, String account) {
        super();
        this.senderId = senderId;
        this.content = content;
        this.newMsgTime = newMsgTime;
        this.account = account;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getNewMsgTime() {
        return newMsgTime;
    }

    public void setNewMsgTime(Date newMsgTime) {
        this.newMsgTime = newMsgTime;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}

