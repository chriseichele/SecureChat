package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

public class Contact {
	
	private String name;
	private String id;
	private String public_key;
	
	private static ArrayList<Contact> contactList;

	public Contact(String id, String name) {
		this.name = name;
		this.id = id;
		getContactDetails();
	}
	
	public void getContactDetails() {
		//fehlende Kontact Details aus XML oder vom Server holen
		
		// TODO
		// public key
	}

	public String getName() {
		return this.name;
	}
	public String getID() {
		return this.id;
	}
	public String getPublicKey() {
		return this.public_key;
	}
	
	public static ArrayList<Contact> getContacts() {

		// ContactListe aus XML oder vom Server holen
		contactList = new ArrayList<Contact>();
		// TODO
        readContacts();
		
		return contactList;
	}
	private static void readContacts() {
		contactList.clear();
        contactList.add(new Contact("1", "Chris"));
        contactList.add(new Contact("2", "Dennis"));
        contactList.add(new Contact("3", "Flo"));
        contactList.add(new Contact("4", "Martin"));
        contactList.add(new Contact("5", "Schnulf"));
        contactList.add(new Contact("6", "Vera"));
	}
	public static String getContactName(String id) {
		String n = "";
		if(id.equals("1")) {
			n = "Chris";
		}
		else if(id.equals("2")) {
			n = "Dennis";
		}
		else if(id.equals("3")) {
			n = "Flo";
		}
		else if(id.equals("4")) {
			n = "Martin";
		}
		else if(id.equals("5")) {
			n = "Schnulf";
		}
		else if(id.equals("6")) {
			n = "Vera";
		}
		return n;
	}
}