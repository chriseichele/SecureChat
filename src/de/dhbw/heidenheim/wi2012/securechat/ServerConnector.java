package de.dhbw.heidenheim.wi2012.securechat;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

public class ServerConnector {
	
	private static Key key;

	public ServerConnector() {
		// TODO Auto-generated constructor stub
	}
	
	public static Key getFileEncryptionKey() {
		
		//TODO Get Key from Server instead of generating locally. This also solves current Problem with new Key on every new startup
		if(key == null) {
			KeyGenerator keygen;
			try {
				keygen = KeyGenerator.getInstance("AES");
				key = keygen.generateKey();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    
		return key;
	}

}
