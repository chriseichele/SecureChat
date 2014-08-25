package de.dhbw.heidenheim.wi2012.securechat.exceptions;

public class ContactNotExistException extends Exception {

	static final long serialVersionUID = 42L; //Declared to supress warning
	
	public ContactNotExistException() {

	}

	public ContactNotExistException(String detailMessage) {
		super(detailMessage);

	}

	public ContactNotExistException(Throwable throwable) {
		super(throwable);

	}

	public ContactNotExistException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
