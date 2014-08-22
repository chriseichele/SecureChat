package de.dhbw.heidenheim.wi2012.securechat;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import android.util.Log;

import com.sun.jersey.api.client.*;

import javax.ws.rs.core.MediaType;
import javax.crypto.KeyGenerator;

public class ServerConnector {
	
	private static Key key;

	public ServerConnector() {
		// TODO Auto-generated constructor stub
	}
	
	public void dummy_connect() {
		
		//TODO It's just a Dummy
		
		try {
			
		Log.d("SecureChat", "Connection test starting...");
		
		WebResource service = Client.create()
								.resource("http://wwi12-01.dhbw-heidenheim.de/SecureChat/webresources");
		Log.d("SecureChat", service.path("entities.user")
								.path("2")
								.accept(MediaType.APPLICATION_XML)
								.get(String.class));

		Log.d("SecureChat", "Connection test finished.");
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
	}
	
	public static Key getFileEncryptionKey() {
		
		//Testverindung aufbauen
		new ServerConnector().dummy_connect();
		
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
