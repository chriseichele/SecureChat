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
import java.util.Date;
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
import org.w3c.dom.Node;
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
		private String output;

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
					
					urlConnection.setDoOutput(true);

					urlConnection.setRequestProperty( "Content-Type", "application/json; charset=utf8" );
					//urlConnection.setRequestProperty( "Content-Length", Integer.toString(urls[1].length()) );

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
				if (this.responseCode == HttpURLConnection.HTTP_OK) {
					inputStream = urlConnection.getInputStream();
				} else {
					//inputStream = urlConnection.getErrorStream(); //Alternativ Error Input lesen
					throw new RuntimeException("HTTP Error "+ this.responseCode);
				}

				// Process the response
				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
				    total.append(line);
				}
				this.output = total.toString();
				return total.toString();//TODO funktioniert aus unerfindlichen gründen nicht...
				
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

			//if (this.exception == null) {
				// Log.d("SecureChat", "URL Call finished successfully.");
				// Daten irgendwo hin schreiben, sodass sie vom UI Task weiter verarbeitet werden koennen
			//} else {
				// Log.d("SecureChat", "URL Call finished with Exception.");
				// Exception behandeln...
			//}
		}

		public Exception getException() {
			return this.exception;
		}
		public int getResponseCode() {
			return this.responseCode;
		}
		public String getOutput() {
			return this.output;
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
				result = task.getOutput();
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
				//Do nothing -> Use normal Android ID instead of hash as Fallback
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
					Node item = attributes.item(i);
					String name = item.getNodeName();
					if(name != null && name.equals("privateKey")) {
						//get key String out of XML
						pks = item.getChildNodes().item(0).getNodeValue();
					}
					if(name != null && name.equals("username")) {
						//get username String out of XML
						username = item.getChildNodes().item(0).getNodeValue();
					}
				}  
			} catch (ParserConfigurationException
					| SAXException 
					| IOException e) {
				throw new ConnectionFailedException("Error parsing privateKey!");
			}

			//parse key String to Key Object
			Key key = GlobalHelper.getRSAKey(pks);

			//TODO Return User Object with new fetched private Key of User
			return new Self(user_id, username, key);

		} catch (NoContentException e) {
			//Kein Inhalt -> Kontakt existiert nicht
			throw new ContactNotExistException();
		}
	}

	public Self registerUser(String username, String pw_hash, boolean send_private_key) throws ConnectionFailedException {
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
			//Generate JSON Data for new User
			JSONObject json = new JSONObject();
			json.put("password_hash", pw_hash);
			if(send_private_key) {
				//Privater Schlüssel nur an den Server Senden, wenn vom Benutzer gewünscht
				json.put("privateKey", GlobalHelper.getRSAString(privateKey));
			}

			//Post Aufruf mit JSON als Parameter
			xml = postXML(this.protokoll + this.login_server_directory + "userloginserver" , json.toString());

		} catch (JSONException
				| NoContentException e) {
			throw new ConnectionFailedException("Error Sending JSON Data for POST to user server");
		}

		//parse XML Data and get new User ID
		String user_id = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));

			Document doc = db.parse(is);
			NodeList attributes = doc.getDocumentElement().getChildNodes();
			for(int i=0;i<attributes.getLength();i++) {
				Node item = attributes.item(i);
				String name = item.getNodeName();
				if(name != null && name.equals("id")) {
					//get ID String out of XML
					user_id = item.getChildNodes().item(0).getNodeValue();
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
			//Generate JSON Data for new User
			JSONObject json = new JSONObject();
			json.put("userloginserver_ID", user_id);
			json.put("public_key", GlobalHelper.getRSAString(publicKey));
			json.put("username", username);

			//Post Aufruf mit JSON als Parameter
			postXML(this.protokoll + this.message_server_directory + "usermessageserver" , json.toString());

		} catch (JSONException 
				| NoContentException e ) {
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
			String xml = getXML(this.protokoll + this.message_server_directory + "message/reciever/" + userID + "/" + timestampLastMessage );

			//TODO parse Messages as objects
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(xml));

				Document doc = db.parse(is);
				NodeList childs = doc.getDocumentElement().getChildNodes();
				for(int i=0;i<childs.getLength();i++) {
					Node item = childs.item(i);
					String name = item.getNodeName();
					if(name != null && name.equals("message")) {
						NodeList attributes = item.getChildNodes();
						String content = null;
						String datetime = null;
						boolean is_mine = false;
						String sender_id = null;
						String reciever_id = null;
						for(int j=0;j<attributes.getLength();j++) {
							Node item2 = childs.item(j);
							String name2 = item2.getNodeName();
							if(name2 != null && name.equals("content")) {
								content = item2.getChildNodes().item(0).getNodeValue();
							}
							if(name2 != null && name.equals("datetime")) {
								datetime = item2.getChildNodes().item(0).getNodeValue();
							}
							if(name2 != null && name.equals("messsagePK")) {
								NodeList sub_attributes = item2.getChildNodes();
								for(int k=0;k<sub_attributes.getLength();k++) {
									Node item3 = sub_attributes.item(k);
									String name3 = item3.getNodeName();
									if(name3 != null && name3.equals("recieverID")) {
										reciever_id = item3.getChildNodes().item(0).getNodeValue();
									}
									if(name3 != null && name3.equals("senderID")) {
										sender_id = item3.getChildNodes().item(0).getNodeValue();
										is_mine = sender_id.equals(userID);
									}
								}
								content = item2.getChildNodes().item(0).getNodeValue();
							}
						}
						messages.add(new Message(content, 
												 is_mine, 
												 sender_id, 
												 reciever_id, 
												 new Date(Long.parseLong(datetime)) ));
					}
				}  
			} catch (ParserConfigurationException
					| SAXException 
					| IOException e) {
				throw new ConnectionFailedException("Error parsing new messages!");
			}

			//TODO remove testing code
			/*for (int i=0;i<2;i++) {
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
			}*/
			
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
			json.put("content", m.getMessage());
			json.put("datetime", m.getTimestamp()+"");
			json.put("reciever_ID", m.getRecieverID());
			json.put("sender_ID", m.getSenderID());

			//Post Aufruf mit XML als Parameter
			postXML(this.protokoll + this.message_server_directory + "message" , json.toString());

		} catch (JSONException
				| NoContentException e2) {
			//throw Exception on Failure
			throw new ConnectionFailedException("Error parsing JSON to send Message!");
		}
	}

}
