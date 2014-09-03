package de.dhbw.heidenheim.wi2012.securechat.exceptions;

public class NoContentException extends Exception {
	
	static final long serialVersionUID = 42L; //Declared to supress warning

	public NoContentException() {

	}

	public NoContentException(String detailMessage) {
		super(detailMessage);

	}

	public NoContentException(Throwable throwable) {
		super(throwable);

	}

	public NoContentException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
