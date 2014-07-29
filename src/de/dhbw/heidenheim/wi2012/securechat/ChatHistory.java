package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;
import java.util.Date;

public class ChatHistory {

	private String user_id;
	private String user_name;
	ArrayList<Message> messages;
	
	public ChatHistory(String user_id) {
		this.user_id = user_id;
		this.user_name = Contact.getContactName(user_id);
	}
	
	public ArrayList<Message> getCurrentMessages() {
		messages = new ArrayList<Message>();
		
		//Get Messages for current Chat from XML

		add(new Message("Hey", false, new Date(1406066828000L)));
		add(new Message("Hi "+this.user_name, true, new Date(1406146928000L)));
		add(new Message("Wie gehts??", false, new Date(1406156958000L)));
		add(new Message("Ganz gut ;)", true, new Date(1406207028000L)));
		add(new Message("arbeite gerade an einer sicheren Chat-App", true, new Date(1406267128000L)));
		add(new Message("Wow, cool!", false, new Date(1406267338000L)));
		
		//Return Message ArrayList
		return messages;
	}
	
	public void add(Message m) {
		//Add Message to Current List
		messages.add(m);
		//Add Message to XML
		
	}
}