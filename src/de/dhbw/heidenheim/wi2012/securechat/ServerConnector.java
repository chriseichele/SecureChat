package de.dhbw.heidenheim.wi2012.securechat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.scheme.SchemeRegistry;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;

public class ServerConnector {
	
	private static Key key;
	private Context context;
	private int connection_trys;
	private String protokoll;
	private String login_server_directory;
	private String message_server_directory;

	public ServerConnector(Context context,int max_connection_trys) {
		this.context = context;
		this.connection_trys = max_connection_trys;
		//Selber Server, da nur einer von der DHBW zur Verfuegung gestellt wurde
		this.protokoll = "https://";
		this.login_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/";
		this.message_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/";
	}
	public ServerConnector(Context context) {
		this(context, 2); //Default max_connection_trys
	}

	/* Verbindungsaufbau Vorlage bei REST von Flo - hat leider nicht funktioniert
	public void testConnect (){
		 Userloginserver user = new Userloginserver();
	        //user.setPasswordHash("PASSWORT-HASH5");
	        //user.setPrivateKey("PRIVATE_KEY");
	        user.setId(80); // muss gesetzt sein, da PK.
	 
	        WebResource service = Client.create().resource("http://wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/entities.userloginserver/2");
	 
	        user = service.path("entities.user").path("2").accept(MediaType.APPLICATION_XML).get(Userloginserver.class);
	        Log.d("userdata", "id: " + user.getId() + " pw: " + user.getPasswordHash() + " key: " + user.getPrivateKey());
	        user.setPasswordHash("TEST");
	        Log.d("userdata",service.path("entities.user").path("2").type(MediaType.APPLICATION_XML).put(String.class, user));
	        Log.d("userdata","id: " + user.getId() + " pw: " + user.getPasswordHash() + " key: " + user.getPrivateKey());
		
	}
	*/

	//Assynchroner Task um XML Datei von URL zu holen
	private class RetrieveXMLTask extends AsyncTask<String, Void, String> {
	    private Exception exception;
	    
	    protected String doInBackground(String... urls) {
	    	try {
	    		
	    		char[] pw = "Collyn24".toCharArray();
	    		
	    		KeyStore keyStore_trust = KeyStore.getInstance("BKS");
	    		InputStream is = context.getResources().openRawResource(R.raw.client_keystore);
	    		BufferedInputStream bis = new BufferedInputStream(is);
	    		keyStore_trust.load(bis, pw);
	    		Key key = keyStore_trust.getKey("client", pw);
	    		bis.close();

	    		// Create a TrustManager that trusts the CAs in our KeyStore
	    		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
	    		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
	    		tmf.init(keyStore_trust);

	    		//Create Key Manager Factory for Client Key
		        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		        KeyStore keyStore_key = KeyStore.getInstance("PKCS12");
		    	is = context.getResources().openRawResource(R.raw.client);
		    	bis = new BufferedInputStream(is);
		        keyStore_key.load(bis, pw);
		        bis.close();
		        kmf.init(keyStore_key, pw);

	    		// Create an SSLContext that uses our TrustManager
	    		SSLContext ssl = SSLContext.getInstance("TLS");
	    		//ssl.init(null, tmf.getTrustManagers(), null);
		        //ssl.init(kmf.getKeyManagers(), null, new SecureRandom());
		        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

	    		// Tell the URLConnection to use a SocketFactory from our SSLContext
	    		URL url = new URL(urls[0]);
	    		HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
	    		urlConnection.setSSLSocketFactory(ssl.getSocketFactory());
	    		urlConnection.setRequestMethod("GET");
	    		urlConnection.setRequestProperty("Accept", "application/xml");
	    		
	    		// Check for errors
		          int responseCode = urlConnection.getResponseCode(); //TODO UnknownHostException
		          InputStream inputStream;
		          if (responseCode == HttpURLConnection.HTTP_OK) {
		            inputStream = urlConnection.getInputStream();
		          } else {
		            inputStream = urlConnection.getErrorStream();
		          }

		          // Process the response
		          BufferedReader reader;
		          String line = null;
		          String output = "";
		          reader = new BufferedReader( new InputStreamReader( inputStream ) );
		          while( ( line = reader.readLine() ) != null )
		          {
		        	  output += line;
		          }

		          inputStream.close();

		          //HTTP Body = XML zurueck geben
		          return output;
	    		
	    	} catch (Exception e) {
	            this.exception = e;
	            return null;
	    	}
	    }
	    
	    /*
	    protected String doInBackground_http(String... urls) {
	        try {
	        	
	        	//Hat funktioniert, jedoch lässt der server keine HTTP Verbindungen mehr zu
	        	
	        	URL url = new URL(urls[0]);
	    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    		conn.setRequestMethod("GET");
	    		conn.setRequestProperty("Accept", "application/xml");
	     
	    		if (conn.getResponseCode() != 200) {
	    			throw new RuntimeException("Failed : HTTP error code : "
	    					+ conn.getResponseCode());
	    		}
	     
	    		BufferedReader br = new BufferedReader(new InputStreamReader(
	    			(conn.getInputStream())));

	    		String line;
	    		String output = "";
	    		while ((line = br.readLine()) != null) {
	    			output += line;
	    		}
	     
	    		conn.disconnect();
	     
	        	//HTTP Body = XML zurueck geben
	    		return output;
	        	
	        } catch (IOException e) {
	            this.exception = e;
	            return null;
	        }
	    }
	    */
	    
	    protected void onPostExecute(String s) {
	        // TODO: check this.exception 
	        // TODO: do something with the feed
	    	
			Log.d("SecureChat", "URL Call finished.");
			
			// -> durch Aufruf von .get() synchron geschaltet
			// asynchrone verarbeitung nicht notwendig.
			
	    	if (this.exception == null) {
	    		//Daten irgendwo hin Schreiben, sodass sie vom UI Task weiter verarbeitet werden koennen
	    	} else {
	    		//Exception behandeln...
	    	}
	    }
	}
	
	private String getXML(String url) throws ConnectionFailedException {
		
		try {
	
			int trys = this.connection_trys;
			String result = null;
			//2 Versuche für Verbindungsaufbau
			while ((result == null || result.trim().isEmpty()) && trys > 0) {
				result = new RetrieveXMLTask().execute(url).get();
				trys--;
			}
			if(result == null || result.trim().isEmpty()) {
				//Kein Ergebnis -> Aufruf fehlgeschlagen
				throw new ConnectionFailedException("Connection to Server Failed!");
			} else {
				return result;
			}
			
		} catch (IllegalArgumentException
				| ExecutionException
				| InterruptedException e) {
			e.printStackTrace();
			throw new ConnectionFailedException("Connection to Server Failed!");
		}
		
	}
	
	public Key getPrivateKey(String user_id) throws ConnectionFailedException {
		//Get XML from server
		String xml = getXML(this.protokoll + this.login_server_directory + "entities.userloginserver/" + user_id);
		
		//TODO parse XML
		//TODO get key String out of XML
		//TODO parse key String to Key Object

		String pks = "TEST";
		byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		Key key = new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA"); 
		
		//Return Key Object
		return key;
	}

	public ArrayList<Message> getNewMessages(Long timestampLastMessage, String userID) throws ConnectionFailedException {
		//TODO retrieve Messages newer as timestamp for current user id from server
		//TODO parse Messages as objects
		return new ArrayList<Message>();
	}
	
	public Key getFileEncryptionKey() throws ConnectionFailedException {
		
		//TODO Get Key for Local Encryption from Server
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
