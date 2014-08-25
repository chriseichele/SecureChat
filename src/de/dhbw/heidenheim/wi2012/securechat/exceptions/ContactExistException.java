package de.dhbw.heidenheim.wi2012.securechat.exceptions;

public class ContactExistException extends Exception {
	
	static final long serialVersionUID = 42L; //Declared to supress warning

	public ContactExistException() {

	}

	public ContactExistException(String detailMessage) {
		super(detailMessage);

	}

	public ContactExistException(Throwable throwable) {
		super(throwable);

	}

	public ContactExistException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
