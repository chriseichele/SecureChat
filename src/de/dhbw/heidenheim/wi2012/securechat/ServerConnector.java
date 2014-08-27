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
	    		/*
	    		// Load CAs from an InputStream
	    		// (could be from a resource or ByteArrayInputStream or ...)
	    		CertificateFactory cf = CertificateFactory.getInstance("X.509");
	    		// From https://www.washington.edu/itconnect/security/ca/load-der.crt
	    		InputStream caInput = new BufferedInputStream(context.getResources().openRawResource(R.raw.cacerts));
	    		Certificate ca;
	    		try {
	    		    ca = cf.generateCertificate(caInput);
	    		    Log.d("CA","ca=" + ((X509Certificate) ca).getSubjectDN());
	    		} finally {
	    		    caInput.close();
	    		}

	    		// Create a KeyStore containing our trusted CAs
	    		String keyStoreType = KeyStore.getDefaultType();
	    		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
	    		keyStore.load(null, null);
	    		keyStore.setCertificateEntry("ca", ca);
	    		*/
	    		/*
	    		KeyStore keyStore = KeyStore.getInstance("PKCS12");
	    		InputStream is = context.getResources().openRawResource(R.raw.cacerts);
	    		BufferedInputStream bis = new BufferedInputStream(is);
	    		String password = "Collyn24";
	    		keyStore.load(bis, password.toCharArray()); // password is the PKCS#12 password. If there is no password, just pass null
	    		*/
	    		KeyStore keyStore = KeyStore.getInstance("BKS");
	    		InputStream is = context.getResources().openRawResource(R.raw.client_keystore);
	    		BufferedInputStream bis = new BufferedInputStream(is);
	    		char[] pw = "Collyn24".toCharArray();
	    		keyStore.load(bis, pw);
	    		Key key = keyStore.getKey("client", pw);
	    		is.close();

	    		// Create a TrustManager that trusts the CAs in our KeyStore
	    		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
	    		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
	    		tmf.init(keyStore);

	    		// Create an SSLContext that uses our TrustManager
	    		SSLContext context = SSLContext.getInstance("TLS");
	    		context.init(null, tmf.getTrustManagers(), null);

	    		// Tell the URLConnection to use a SocketFactory from our SSLContext
	    		URL url = new URL(urls[0]);
	    		HttpsURLConnection urlConnection =
	    		    (HttpsURLConnection)url.openConnection();
	    		urlConnection.setSSLSocketFactory(context.getSocketFactory());
	    		urlConnection.setRequestMethod("GET");
	    		urlConnection.setRequestProperty("Accept", "application/xml");
	    		
	    		// Check for errors
		          int responseCode = urlConnection.getResponseCode(); //TODO Bis jetzt noch keine Antwort
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
	    
	    protected String doInBackground2(String... urls) {
	    	// Use the public key from the AIDAP server as the trust store for this client.
	        //   (note: created this keystore using InstallCerts.java from sun.com)
	        Properties systemProps = System.getProperties();
	        systemProps.put( "javax.net.ssl.trustStore", "/d1/cvs_all/jssecacerts");
	        System.setProperties(systemProps);

	        try {
	          // Open a secure connection.
	          URL url = new URL(urls[0]);
	          //String requestParams = "uid=adds&password=aAsS22.q&active=y&type=F";
	          HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

	          // Set up the connection properties
	          con.setRequestProperty( "Connection", "close" );
	          con.setDoInput(true);
	          con.setDoOutput(true);
	          con.setUseCaches(false);
	          con.setConnectTimeout( 30000 );
	          con.setReadTimeout( 30000 );
	          con.setRequestMethod( "GET" );
	    	  con.setRequestProperty("Accept", "application/xml");
	          //con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
	          //con.setRequestProperty( "Content-Length", Integer.toString(requestParams.length()) );

	          // Set up the user authentication portion of the handshake with the private
	          // key provided by NAIMES Tech Support.
	          //   Based on an example posted by Torsten Curdt on his blog:
	          //     http://vafer.org/blog/20061010073725 (as of Nov, 2009)
	          File pKeyFile = new File("/d1/cvs_all/aidapuser_1f5d_2011_03_1192.pfx");
	          String pKeyPassword = "UB#20abba";
	          KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
	          KeyStore keyStore = KeyStore.getInstance("PKCS12");
	          InputStream keyInput = new FileInputStream(pKeyFile);
	          keyStore.load(keyInput, pKeyPassword.toCharArray());
	          keyInput.close();
	          keyManagerFactory.init(keyStore, pKeyPassword.toCharArray());
	          SSLContext context = SSLContext.getInstance("TLS");
	          context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
	          SSLSocketFactory sockFact = context.getSocketFactory();
	          con.setSSLSocketFactory( sockFact );

	          // Send the request
	          //OutputStream outputStream = con.getOutputStream();
	          //outputStream.write( requestParams.getBytes("UTF-8") );
	          //outputStream.close();

	          // Check for errors
	          int responseCode = con.getResponseCode();
	          InputStream inputStream;
	          if (responseCode == HttpURLConnection.HTTP_OK) {
	            inputStream = con.getInputStream();
	          } else {
	            inputStream = con.getErrorStream();
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
	    
	    protected String doInBackground1(String... urls) {
	        try {
	        	
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
