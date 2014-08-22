package de.dhbw.heidenheim.wi2012.securechat;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
		
		WebResource service = Client.create()
									.resource( "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources" );
		System.out.println( service.path( "entities." )
								   .accept( MediaType.APPLICATION_JSON)
								   .get( String.class ) ); 
		
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
