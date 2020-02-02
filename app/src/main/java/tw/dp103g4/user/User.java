package tw.dp103g4.user;

import java.io.Serializable;
import java.sql.Date;

public class User implements Serializable {

    private int id;
	private String account;
	private String password;
	private String email;
	private String name;
	private boolean isOver;
	private Date time;


    public User(int id, String account, String password, String email, String name, boolean isOver, Date time) {
        super();
        this.id = id;
        this.account = account;
        this.password = password;
        this.email = email;
        this.name = name;
        this.isOver = isOver;
        this.time = time;
    }
	
	public User(String account, String password, String email, String name, boolean isOver, Date time) {
		super();
		this.account = account;
		this.password = password;
		this.email = email;
		this.name = name;
		this.isOver = isOver;
		this.time = time;
	}

	public User(String account, String password, String email, String name) {
		this.account = account;
		this.password = password;
		this.email = email;
		this.name = name;
	}

	
	public User(String account, String password) {
		super();
		this.account = account;
		this.password = password;
	}




	public String getAccount() {
		return account;
	}


	public void setAccount(String account) {
		this.account = account;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public boolean isOver() {
		return isOver;
	}


	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}
	
	
	
	

}
