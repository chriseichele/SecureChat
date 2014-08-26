package de.dhbw.heidenheim.wi2012.securechat.exceptions;

public class ConnectionFailedException extends Exception {
	
	static final long serialVersionUID = 42L; //Declared to supress warning

	public ConnectionFailedException() {

	}

	public ConnectionFailedException(String detailMessage) {
		super(detailMessage);

	}

	public ConnectionFailedException(Throwable throwable) {
		super(throwable);

	}

	public ConnectionFailedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
