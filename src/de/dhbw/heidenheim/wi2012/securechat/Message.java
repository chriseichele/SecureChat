package de.dhbw.heidenheim.wi2012.securechat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Message is a Custom Object to encapsulate message information/fields
 * 
 * @author 
 *
 */
public class Message {
	/**
	 * The content of the message
	 */
	String message;
	/**
	 * The timestamp of the message
	 */
	Date datetime;
	/**
	 * boolean to determine, who is sender of this message
	 */
	boolean isMine;
	
	/**
	 * Constructor to make a Message object
	 */
	public Message(String message, boolean isMine, Date datetime) {
		super();
		this.message = message;
		this.isMine = isMine;
		this.datetime = datetime;
	}
	public Message(String message, boolean isMine) {
		super();
		this.message = message;
		this.isMine = isMine;
		Calendar cal = Calendar.getInstance();
		datetime = cal.getTime();
	}
	public String getMessage() {
		return message;
	}
	public String getMessageTime() {
		DateFormat dateFormat;
		if(isToday(this.datetime)) {
			dateFormat= new SimpleDateFormat("HH:mm");
		} else {
			dateFormat= new SimpleDateFormat("dd.MM.yyyy - HH:mm");
		}
		return dateFormat.format(this.datetime);
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isMine() {
		return isMine;
	}
	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}
	
	private boolean isToday(Date datetime) {
		//current Date
		Calendar cal1 = Calendar.getInstance();
		//test date
		Calendar cal2 = Calendar.getInstance();
        cal2.setTime(datetime);
        //return true, if same day
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
}