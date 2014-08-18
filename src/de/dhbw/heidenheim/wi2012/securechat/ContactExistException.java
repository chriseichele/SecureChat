package de.dhbw.heidenheim.wi2012.securechat;

public class ContactExistException extends Exception {

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
