package de.dhbw.heidenheim.wi2012.securechat.exceptions;

public class EncryptionErrorException extends Exception {
	
	static final long serialVersionUID = 42L; //Declared to supress warning

	public EncryptionErrorException() {

	}

	public EncryptionErrorException(String detailMessage) {
		super(detailMessage);

	}

	public EncryptionErrorException(Throwable throwable) {
		super(throwable);

	}

	public EncryptionErrorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
