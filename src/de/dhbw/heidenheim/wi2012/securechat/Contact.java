package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

public class Contact {
	
	private String name;
	private String id;
	private String public_key;

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
		ArrayList<Contact> contactList = new ArrayList<Contact>();
		// TODO
        contactList.add(new Contact("1", "Chris"));
        contactList.add(new Contact("2", "Dennis"));
        contactList.add(new Contact("3", "Flo"));
        contactList.add(new Contact("4", "Martin"));
        contactList.add(new Contact("5", "Schnulf"));
        contactList.add(new Contact("6", "Vera"));
        
		return contactList;
	}
}