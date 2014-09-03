package de.dhbw.heidenheim.wi2012.securechat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Base64;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.NoContentException;

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
		this.login_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/de.dhbwheidenheim.wwi1201.securechat.";
		this.message_server_directory = "wwi12-01.dhbw-heidenheim.de/SecureChat/webresources/de.dhbwheidenheim.wwi1201.securechat.";
	}
	public ServerConnector(Context context) {
		this(context, 2); //Default max_connection_trys
	}

	//Assynchroner Task um XML Datei von URL zu holen
	private class RetrieveXMLTask extends AsyncTask<String, Void, String> {

		private String method;

		public RetrieveXMLTask(String method) {
			this.method = method;
		}

		private Exception exception;
		private int responseCode;

		protected String doInBackground(String... urls) {
			try {

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
				urlConnection.setRequestProperty("Accept", "application/xml");

				if(this.method == "POST") {
					urlConnection.setRequestMethod("POST");

					urlConnection.setRequestProperty( "Content-Type", "application/json; charset=utf8" );
					urlConnection.setRequestProperty( "Content-Length", Integer.toString(urls[1].length()) );

					// Sent the Post Parameter
					OutputStream os = urlConnection.getOutputStream();
					os.write(urls[1].getBytes("UTF-8"));
					os.close();
				} else if(this.method == "GET") {
					urlConnection.setRequestMethod("GET");
				} else {
					//nicht definiert
				}

				// Check for errors
				this.responseCode = urlConnection.getResponseCode();
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
		public int getResponseCode() {
			return this.responseCode;
		}
	}

	private String getXML(String url) throws ConnectionFailedException, NoContentException {
		return connect("GET", url, null);
	}
	private String postXML(String url, String parameter) throws ConnectionFailedException, NoContentException {
		return connect("POST", url, parameter);
	}
	private String connect(String method,String url, String post_parameter) throws ConnectionFailedException, NoContentException {

		try {

			int trys = this.connection_trys;
			String result = null;
			RetrieveXMLTask task = null;
			//Versuche fuer Verbindungsaufbau, bis Return wert vorhanden
			while ((result == null || result.trim().isEmpty()) && trys > 0) {
				task = new RetrieveXMLTask(method);
				result = task.execute(url, post_parameter).get();
				if(task.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
					//Kein Inhalt: Verbindung erfolgreich, abbrechen
					break;
				}
				trys--;
			}
			if(result == null || result.trim().isEmpty()) {
				if(task.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
					//Kein Inhalt
					throw new NoContentException();
				} else {
					//Kein Ergebnis -> Aufruf fehlgeschlagen
					throw new ConnectionFailedException("Connection to Server Failed! " + task.getException().getMessage());
				}
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


	/* ----------------------------------------------------------------------------------------
	 * Ab hier kommen oeffentliche Methoden zum Aufruf der Verbindungen
	 * ----------------------------------------------------------------------------------------
	 */

	public Key getFileEncryptionKey() throws ConnectionFailedException {

		//Key nur vom server holen, wenn nicht bereits lokal zwischengespeichert
		if(key == null) {

			//File Encryption Key ist abhaengig vom Client Geraet
			String android_id = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
			try {
				//Versuche SHA-Hash der ID zu erstellen
				MessageDigest md = MessageDigest.getInstance("SHA-512");
				md.update(android_id.getBytes());
				byte[] bytes = md.digest();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < bytes.length; i++) {
					String tmp = Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
					buffer.append(tmp);
				}
				//ID mit hash ueberschreiben
				android_id = buffer.toString();
			} catch(Exception e) {
				//Do nothing -> Use normal Android ID instead of hash
			}

			//Get Key for Local Encryption from Server (unique for Android Device)
			String xml;
			try {
				//Versuche Schluessel abzurufen
				xml = getXML(this.protokoll + this.login_server_directory + "keystore/" + android_id );
			} catch (ConnectionFailedException 
					| NoContentException e2) {
				//Schluessel fuer ID noch nicht vorhanden -> neuen anlegen
				try {
					//Generate JSON Data
					JSONObject json = new JSONObject();
					json.put("ID", android_id);
					json.put("aeskey", "dummy");

					//Post Aufruf mit XML als Parameter
					xml = postXML(this.protokoll + this.login_server_directory + "keystore" , json.toString());

				} catch (JSONException 
						| NoContentException e) {
					throw new ConnectionFailedException("Error Sending JSON Data for POST");
				}
			}

			//Parse Key as Key Object out of Return XML
			String ks = null; //Key String
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(xml));

				Document doc = db.parse(is);
				ks = doc.getDocumentElement().getNodeValue();

			} catch (ParserConfigurationException
					| SAXException 
					| IOException e) {
				// handle ParserConfigurationException
				throw new ConnectionFailedException("Error parsing FileEncryptionKey!");
			}

			byte[] encodedKey = Base64.decode(ks, Base64.DEFAULT);
			key = new SecretKeySpec(encodedKey,0,encodedKey.length, "AES");
		}

		//Return Key Object
		return key;
	}

	public Self loginUser(String user_id, String pw_hash) throws ConnectionFailedException, ContactNotExistException {

		try {
			//TODO Get XML from server (server checks)
			String xml = getXML(this.protokoll + this.login_server_directory + "userloginserver/" + user_id + pw_hash);

			//parse XML
			String pks = null; //Private Key String
			String username = null;
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(xml));

				Document doc = db.parse(is);
				NodeList attributes = doc.getDocumentElement().getChildNodes();
				for(int i=0;i<attributes.getLength();i++) {
					if(attributes.item(i).getLocalName().equals("privateKey")) {
						//get key String out of XML
						pks = attributes.item(i).getNodeValue();
					}
					if(attributes.item(i).getLocalName().equals("username")) {
						//get username String out of XML
						username = attributes.item(i).getNodeValue();
					}
				}  
			} catch (ParserConfigurationException
					| SAXException 
					| IOException e) {
				throw new ConnectionFailedException("Error parsing privateKey!");
			}

			pks = "7oN8K0sTDas700OKt8tThM2o"; //TODO remove dummy key string
			username = "Dummy"; //TODO remove dummy name string

			//parse key String to Key Object
			Key key = GlobalHelper.getRSAKey(pks);

			//TODO Return User Object with new fetched private Key of User
			return new Self(user_id, username, key);

		} catch (NoContentException e) {
			//Kein Inhalt -> Kontakt existiert nichts
			throw new ContactNotExistException();
		}
	}

	public Self registerUser(String username, String pw_hash) throws ConnectionFailedException {
		//Generieren von RSA Keypaar (private_key & public_key)
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
		} catch(NoSuchAlgorithmException e) {
			throw new ConnectionFailedException(e.getMessage());
		}

		//User bei userloginserver registrieren (mit Username, password & private_key) -> return ID
		String xml = null;
		try {
			//TODO Generate JSON Data for new User
			JSONObject json = new JSONObject();
			json.put("username", username);
			json.put("password", pw_hash);
			json.put("privateKey", privateKey);

			//Post Aufruf mit XML als Parameter
			xml = postXML(this.protokoll + this.login_server_directory + "userloginserver" , json.toString());

		} catch (JSONException
				| NoContentException e) {
			throw new ConnectionFailedException("Error Sending JSON Data for POST to user server");
		}

		//TODO parse XML Data and get new User ID
		String user_id = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));

			Document doc = db.parse(is);
			NodeList attributes = doc.getDocumentElement().getChildNodes();
			for(int i=0;i<attributes.getLength();i++) {
				if(attributes.item(i).getLocalName().equals("ID")) {
					//get ID String out of XML
					user_id = attributes.item(i).getNodeValue();
					break;
				}
			}  
		} catch (ParserConfigurationException
				| SAXException 
				| IOException e) {
			throw new ConnectionFailedException("Error parsing user ID!");
		}

		//User bei usermessageserver registrieren (mit ID, Username & public_key)
		try {
			//TODO Generate JSON Data for new User
			JSONObject json = new JSONObject();
			json.put("ID", user_id);
			json.put("username", username);
			json.put("publicKey", publicKey);

			//Post Aufruf mit XML als Parameter
			xml = postXML(this.protokoll + this.login_server_directory + "userloginserver" , json.toString());

		} catch (JSONException
				| NoContentException e) {
			throw new ConnectionFailedException("Error Sending JSON Data for POST to message server");
		}

		//Return User Object
		return new Self(user_id, username, privateKey);
	}

	public Contact getContact(String contactID) throws ConnectionFailedException, ContactNotExistException {
		//Get Contact Details from Server (Name & public Key)
		try {
			String xml = getXML(this.protokoll + this.message_server_directory + "usermessageserver/" + contactID );
		} catch (NoContentException e) {
			//Kontakt nicht gefunden
			throw new ContactNotExistException();
		}

		//TODO Parse XML Contact Data Object
		String name = null;

		//TODO Remove Dummy Code
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

		ArrayList<Message> messages = new ArrayList<Message>();

		try {
			//TODO retrieve Messages newer as timestamp for current user id from server
			String xml = getXML(this.protokoll + this.message_server_directory + "message/" + timestampLastMessage + userID );

			//TODO parse Messages as objects

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
		} catch (NoContentException e) {
			//Do Nothing
			//Return empty message Array
		}

		//Return Array with new Messages
		return messages;
	}

	public void sendMessage(Message m) throws ConnectionFailedException {
		//send Message to server
		try {
			//TODO Generate JSON Data for new Message
			JSONObject json = new JSONObject();
			json.put("timestamp", m.getTimestamp()+"");
			json.put("sender", m.getSenderID());
			json.put("reciever", m.getRecieverID());
			json.put("content", m.getMessage());

			//Post Aufruf mit XML als Parameter
			postXML(this.protokoll + this.message_server_directory + "message" , json.toString());

		} catch (JSONException
				| NoContentException e2) {
			//throw Exception on Failure
			throw new ConnectionFailedException("Error parsing JSON to send Message!");
		}
	}

}
