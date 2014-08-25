package de.dhbw.heidenheim.wi2012.securechat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
	String sender;
	
	/**
	 * Constructor to make a Message object
	 */
	public Message(String message, boolean isMine, Date datetime) {
		this.message = message;
		this.isMine = isMine;
		this.datetime = datetime;
	}
	public Message(String message, boolean isMine) {
		this(message, isMine, Calendar.getInstance().getTime());
	}
	public Message(String message, String sender_id, Date datetime) {
		this(message, false, datetime);
		this.sender = sender_id;
	}
	public Message(String message, String sender_id) {
		this(message, false);
		this.sender = sender_id;
	}
	public String getMessage() {
		return message;
	}
	public long getTimestamp() {
		return this.datetime.getTime();
	}
	public String getMessageTime() {
		DateFormat dateFormat;
		// Datum/Zeit nach lokalem Standard formatieren
		if(isToday(this.datetime)) {
			//Wenn heute, nur Zeit
			dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
		} else {
			//Wenn vergangener Tag, Datum und Uhrzeit
			dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
		}
		//return Datum/Zeit der Nachricht formatiert als String
		return dateFormat.format(this.datetime);
	}
	public boolean isMine() {
		return isMine;
	}
	public void setSender(String s) {
		this.sender = s;
	}
	public String getSender() {
		return this.sender;
	}
	
	private boolean isToday(Date datetime) {
		//current Date
		Calendar cal1 = Calendar.getInstance();
		//Date to Text
		Calendar cal2 = Calendar.getInstance();
        cal2.setTime(datetime);
        //return true, if same day
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
}