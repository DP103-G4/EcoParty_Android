package tw.dp103g4.news;

import java.io.Serializable;
import java.sql.Date;

public class News implements Serializable {
    private int id;
    private String title;
    private String content;
    private Date time;

    public News(int id, String title, String content, Date time) {
        super();
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

}
