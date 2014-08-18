package de.dhbw.heidenheim.wi2012.securechat;

public class ContactNotExistException extends Exception {

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
