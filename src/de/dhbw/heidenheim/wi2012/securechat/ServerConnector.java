package de.dhbw.heidenheim.wi2012.securechat;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import android.os.AsyncTask;
import android.util.Log;

import com.sun.jersey.api.client.*;

import javax.ws.rs.core.MediaType;
import javax.crypto.KeyGenerator;

public class ServerConnector {
	
	private static Key key;

	public ServerConnector() {
		// TODO Auto-generated constructor stub
	}

	private class RetrieveXMLTask extends AsyncTask<String, Void, String> {
	    private Exception exception;
	    protected String doInBackground(String... urls) {
	        try {
	
	    		WebResource service = Client.create().resource(urls[0]);
	    		//return service.path("").accept(MediaType.APPLICATION_XML).get(String.class);
	    		return (service.path("entities.user").path("2").accept(MediaType.APPLICATION_XML).get(String.class));
	            
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
	    }
	    protected void onPostExecute(String xml) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
	    	

			Log.d("SecureChat", "URL Call finished.");
			
	    	if (this.exception == null) {
	    		//Daten irgendwo hin Schreiben, sodass sie vom UI Task weiter verarbeitet werden koennen
	    	} else {
	    		//Exception behandeln...
	    	}
	    }
	}
	
	public void dummy_connect() {
		//TODO It's just a Dummy
		
		try {
			
		Log.d("SecureChat", "Connection test starting...");

		new RetrieveXMLTask().execute("http://wwi12-01.dhbw-heidenheim.de/SecureChat/webresources");

		Log.d("SecureChat", "Connection test finished.");
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
	}
	
	public ArrayList<Message> getNewMessages(Long timestampLastMessage, String userID) {
		//TODO retrieve Messages newer as timestamp for current user id from server
		//TODO parse Messages as objects
		return new ArrayList<Message>();
	}
	
	public static Key getFileEncryptionKey() {
		
		//Testverindung aufbauen
		new ServerConnector().dummy_connect();
		
		//TODO Get Key out of PIN for Local Encryption
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
