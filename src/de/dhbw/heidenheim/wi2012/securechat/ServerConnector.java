package de.dhbw.heidenheim.wi2012.securechat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;

public class ServerConnector {

	private static Key key;
	private Context context;
	private int connection_trys;
	private String protokoll;
	private String login_server_directory;
	private String message_server_directory;

	public ServerConnector(Context context, int max_connection_trys) {
		this.context = context;
		if(max_connection_trys < 1) {
			this.connection_trys = 1;
		} else {
			this.connection_trys = max_connection_trys;
		}
		
		this.protokoll = "https://";
		//Selber Server, da nur einer von der DHBW zur Verfuegung gestellt wurde
		this.login_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/";
		this.message_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/";
	}
	public ServerConnector(Context context) {
		this(context, 2); //Default max_connection_trys
	}

	//Assynchroner Task um XML Datei von URL zu holen
	private class RetrieveXMLTask extends AsyncTask<String, Void, String> {
		
		private Exception exception;

		protected String doInBackground(String... urls) {
			try {
				
				urls[0] = "https://wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/entities.message"; //TODO currently working URL -> remove this line later

				char[] pw = "changeit".toCharArray();

				//Truststore
				KeyStore trustStore = KeyStore.getInstance("BKS");
				InputStream is = context.getResources().openRawResource(R.raw.truststore);
				BufferedInputStream bis = new BufferedInputStream(is);
				trustStore.load(bis, pw);
				bis.close();

				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(trustStore);

				//Keystore
				KeyStore keyStore = KeyStore.getInstance("BKS");
				is = context.getResources().openRawResource(R.raw.keystore);
				bis = new BufferedInputStream(is);
				keyStore.load(bis, pw);
				bis.close();

				KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keyStore, pw);

				// Create an SSLContext that uses our TrustManager
				SSLContext ssl = SSLContext.getInstance("TLS");
				ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

				// Tell the URLConnection to use a SocketFactory from our SSLContext
				URL url = new URL(urls[0]);
				HttpsURLConnection urlConnection = (HttpsURLConnection)url.openConnection();
				urlConnection.setSSLSocketFactory(ssl.getSocketFactory());
				urlConnection.setRequestMethod("GET");
				urlConnection.setRequestProperty("Accept", "application/xml");

				// Send the request
				//OutputStream outputStream = urlConnection.getOutputStream();
				//outputStream.write( "Output Stream Content".getBytes("UTF-8") );
				//outputStream.close();

				// Check for errors
				int responseCode = urlConnection.getResponseCode();
				InputStream inputStream;
				if (responseCode == HttpURLConnection.HTTP_OK) {
					inputStream = urlConnection.getInputStream();
				} else {
					//inputStream = urlConnection.getErrorStream(); //Alternativ Error Input lesen
					throw new RuntimeException("HTTP Error "+ responseCode);
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

		protected void onPostExecute(String s) {
			// check this.exception 
			// do something with the feed


			// -> durch Aufruf von .get() synchron geschaltet
			// asynchrone verarbeitung nicht notwendig.

			if (this.exception == null) {
				// Log.d("SecureChat", "URL Call finished successfully.");
				// Daten irgendwo hin schreiben, sodass sie vom UI Task weiter verarbeitet werden koennen
			} else {
				// Log.d("SecureChat", "URL Call finished with Exception.");
				// Exception behandeln...
			}
		}
		
		public Exception getException() {
			return this.exception;
		}
	}

	private String getXML(String url) throws ConnectionFailedException {

		try {

			int trys = this.connection_trys;
			String result = null;
			RetrieveXMLTask task = null;
			//Versuche für Verbindungsaufbau
			while ((result == null || result.trim().isEmpty()) && trys > 0) {
				task = new RetrieveXMLTask();
				result = task.execute(url).get();
				trys--;
			}
			if(result == null || result.trim().isEmpty()) {
				//Kein Ergebnis -> Aufruf fehlgeschlagen
				throw new ConnectionFailedException("Connection to Server Failed! " + task.getException().getMessage());
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

	public Key getFileEncryptionKey() throws ConnectionFailedException {

		//TODO Get Key for Local Encryption from Server
		//TODO Parse Key as Key Object
		
		if(key == null) {
			//KeyGenerator keygen;
			//keygen = KeyGenerator.getInstance("AES");
			//key = keygen.generateKey();

			String pks = "E61F8A266A2D876CCE172415386550HK";
			byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
			key = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");
		}

		return key;
	}
	
	public Self loginUser(String user_id, String pw_hash) throws ConnectionFailedException, ContactNotExistException {
		//TODO check user data on server
		if (false) {
			//If Contact does not exist or wrong login details
			throw new ContactNotExistException();
		}
		//TODO get username form server
		String username = "Dummy";

		//Return User Object with new fetched private Key of User
		return new Self(user_id, username, getPrivateKey(user_id));
	}
	
	public Self registerUser(String username, String pw_hash) throws ConnectionFailedException {
		//TODO register User at Server
		//TODO get new User ID
		String user_id = "0";

		//Return User Object with new fetched private Key of User
		return new Self(user_id, username, getPrivateKey(user_id));
	}

	private Key getPrivateKey(String user_id) throws ConnectionFailedException {
		//Get XML from server
		String xml = getXML(this.protokoll + this.login_server_directory + "entities.userloginserver/" + user_id);

		//TODO parse XML
		//TODO get key String out of XML
		String pks = "7oN8K0sTDas700OKt8tThM2o";
		
		//parse key String to Key Object
		Key key = GlobalHelper.getRSAKey(pks);

		//Return Key Object
		return key;
	}

	public Contact getContact(String contactID) throws ConnectionFailedException, ContactNotExistException {
		//TODO Get Contact Details from Server (Name & public Key)
		//TODO Parse As Object

		
		//TODO Remove Dummy Code
		String name;
		if (contactID.equals("0")) {
			name = "Testkontakt";
		}
		else if (contactID.equals("1")) {
			name = "Chris";
		}
		else if (contactID.equals("2")) {
			name = "Dennis";
		}
		else if (contactID.equals("3")) {
			name = "Flo";
		}
		else if (contactID.equals("4")) {
			name = "Martin";
		}
		else if (contactID.equals("5")) {
			name = "Schnulf";
		}
		else if (contactID.equals("6")) {
			name = "Vera";
		}
		else {
			//Kontakt nicht gefunden
			throw new ContactNotExistException();
		}
		
		//Contact Objekt mit Daten zurueck geben
		return new Contact(contactID, name, GlobalHelper.getRSAKey("TESTKEY"));
	}

	public ArrayList<Message> getNewMessages(Long timestampLastMessage, String userID) throws ConnectionFailedException {
		//TODO retrieve Messages newer as timestamp for current user id from server
		//TODO parse Messages as objects
		ArrayList<Message> messages = new ArrayList<Message>();
		
		//TODO remove testing code
		for (int i=0;i<2;i++) {
			if(new Random().nextInt(40) == 0) { 
				messages.add(new Message("Hi :)", false, new Random().nextInt(6)+"", "1"));
			}
			if(new Random().nextInt(40) == 0) { 
				messages.add(new Message("Hey!", false, new Random().nextInt(6)+"", "1"));
			}
			if(new Random().nextInt(30) == 0) { 
				messages.add(new Message("Jemand zuhause?", false, new Random().nextInt(6)+"", "1"));
			}
			if(new Random().nextInt(30) == 0) { 
				messages.add(new Message("Wie geht's dir so?", false, new Random().nextInt(6)+"", "1"));
			}
			if(new Random().nextInt(20) == 0) { 
				messages.add(new Message("Sag mal, wie klappts jetzt mit der App?", false, new Random().nextInt(6)+"", "1"));
			}
		}
		
		//Return Array with new Messages
		return messages;
	}
	
	public void sendMessage(Message m) throws ConnectionFailedException {
		//TODO send Message to server
		//TODO throw Exception on failure
	}

}
