package de.dhbw.heidenheim.wi2012.securechat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.EncryptionErrorException;
import android.content.Context;
import android.util.Base64;
import android.util.Xml;

public class ChatHistory {

	private ArrayList<Message> messages;
	private String filename;
	
	private static PrivateKey own_privateKey;
	private PublicKey sender_publicKey;

	private static final String filename_last_sync = "sync.xml";

	private static Context context;

	private static Cipher cipher_enc;
	private static Cipher cipher_dec;

	public ChatHistory(String user_id, Context appContext) throws ConnectionFailedException {
		context = appContext;
		this.sender_publicKey = null; //erst mal leer lassen, und bei bedarf holen
		this.messages = new ArrayList<Message>();
		this.filename = "chatHistory-"+user_id+".xml";

		//Check if file History for Contact exists
		if(!context.getFileStreamPath(filename).exists()) {

			//Try File Creation
			try {

				//Encrypt with Cipher
				if (cipher_enc == null) {
					cipher_enc = Cipher.getInstance("AES");
					cipher_enc.init(Cipher.ENCRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
				}

				FileOutputStream fos = context.openFileOutput(this.filename, Context.MODE_PRIVATE);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				CipherOutputStream cos = new CipherOutputStream(bos, cipher_enc);

				XmlSerializer serializer = Xml.newSerializer();

				serializer.setOutput(cos, "UTF-8");
				serializer.startDocument(null, Boolean.valueOf(true));
				serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
				serializer.startTag(null, "root");
				serializer.endTag(null, "root");
				serializer.endDocument();
				serializer.flush();
				cos.close();

			} catch (IOException
					| InvalidKeyException 
					| NoSuchAlgorithmException 
					| NoSuchPaddingException  e) {
				// TODO Auto-generated catch block -> do nothing
				e.printStackTrace();
			}
		}
	}

	public ArrayList<Message> getCurrentMessages() throws ConnectionFailedException {

		messages.clear();

		//Get Messages for current Chat from XML

		try {

			//Decrypt with Cipher
			if (cipher_dec == null) {
				cipher_dec = Cipher.getInstance("AES");
				cipher_dec.init(Cipher.DECRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
			}

			FileInputStream fis = context.openFileInput(this.filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			CipherInputStream cis = new CipherInputStream(bis, cipher_dec);

			InputStreamReader isr = new InputStreamReader(cis);
			char[] inputBuffer = new char[fis.available()];
			isr.read(inputBuffer);
			String data = new String(inputBuffer);
			isr.close();
			fis.close();

			/*
			 * converting the String data to XML format
			 * so that the DOM parser understand it as an XML input.
			 */
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));


			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(is);
			NodeList root_item = dom.getElementsByTagName("root");
			if (root_item.item(0).hasChildNodes()) {
				NodeList items = root_item.item(0).getChildNodes();

				for (int i=1;i<(items.getLength()-1);i=i+2){
					NodeList attributes = items.item(i).getChildNodes();
					//Werte auslesen
					boolean is_my_message = (attributes.item(1).getAttributes().item(0).getNodeValue().equals("true"));
					String sender_id = (attributes.item(1).getAttributes().item(1).getNodeValue());
					String reciever_id = (attributes.item(1).getAttributes().item(2).getNodeValue());
					String content = attributes.item(3).getFirstChild().getNodeValue();
					String signature = attributes.item(5).getFirstChild().getNodeValue();
					long timestamp = Long.parseLong(attributes.item(7).getFirstChild().getNodeValue(), 10);
					//Neue Nachricht an Array ausgeben
					messages.add(new Message(content, signature, is_my_message, sender_id, reciever_id, new Date(timestamp)));
				}
			}

		} catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| SAXException 
				| ParserConfigurationException e) {
			// TODO Auto-generated catch block -> do nothing
			e.printStackTrace();
		}

		//Return Message ArrayList
		return messages;
	}

	public Long getLatestMessageTimestamp() throws ConnectionFailedException {
		if(messages == null) {
			messages = getCurrentMessages();
		}
		if(messages != null) {
			Message m;
			int i = 0;
			Long timestamp = 0L;
			//Timestamp der letzten empfangenen Nachricht holen
			do {
				m = messages.get(messages.size()-i);
				timestamp = m.getTimestamp();
				i++;
			} while (m.isMine() && messages.size() >= i );
			return timestamp;
		} else {
			//Keine Nachricht, Timstamp auf sehr alt
			return 0L;
		}
	}
	
	public void send(Message m) throws ConnectionFailedException, EncryptionErrorException {
		//Encrypt
		m = encryptMessageContent(m);
		//Add to server
		new ServerConnector(context).sendMessage(m);//Context sollte hier schon lange initialisiert sein
		//Add to local 
		add(m);
	}
	public void retrieveMessage(Message m) throws ConnectionFailedException, EncryptionErrorException {
		//Encrypt
		m = decryptMessageContent(m);
		//Add to local 
		add(m);
	}

	public synchronized void add(Message m) throws ConnectionFailedException, EncryptionErrorException {
		try {

			//Add Message to Current List
			messages.add(m);

			//Add Message to local XML

			//Decrypt with Cipher
			if (cipher_dec == null) {
				cipher_dec = Cipher.getInstance("AES");
				cipher_dec.init(Cipher.DECRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
			}
			//Encrypt with Cipher
			if (cipher_enc == null) {
				cipher_enc = Cipher.getInstance("AES");
				cipher_enc.init(Cipher.ENCRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
			}

			FileInputStream fis = context.openFileInput(this.filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			CipherInputStream cis = new CipherInputStream(bis, cipher_dec);

			InputStreamReader isr = new InputStreamReader(cis);
			char[] inputBuffer = new char[fis.available()];
			isr.read(inputBuffer);
			String data = new String(inputBuffer);
			isr.close();
			fis.close();

			/*
			 * converting the String data to XML format
			 * so that the DOM parser understand it as an XML input.
			 */
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(is);
			Element root = dom.getDocumentElement();

			Element message_node = dom.createElement("message");

			Element connection = dom.createElement("connection");
			connection.setAttribute("mine", m.isMine()+"");
			connection.setAttribute("sender", m.getSenderID()+"");
			connection.setAttribute("reciever", m.getRecieverID()+"");
			message_node.appendChild(connection);

			Element content = dom.createElement("content");
			content.appendChild(dom.createTextNode(m.getMessage()));
			message_node.appendChild(content);

			Element sign = dom.createElement("signature");
			sign.appendChild(dom.createTextNode(m.getSignature()));
			message_node.appendChild(sign);

			Element timestamp = dom.createElement("timestamp");
			timestamp.appendChild(dom.createTextNode(m.getTimestamp()+""));
			message_node.appendChild(timestamp);

			root.appendChild(message_node);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			FileOutputStream fos = context.openFileOutput(this.filename, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			CipherOutputStream cos = new CipherOutputStream(bos, cipher_enc);

			StreamResult result = new StreamResult(cos);
			DOMSource source = new DOMSource(dom);
			transformer.transform(source, result);
			cos.close();

		}
		catch (TransformerException 
				| IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException  
				| SAXException 
				| ParserConfigurationException e) {
			// TODO Auto-generated catch block -> do nothing
			e.printStackTrace();
			//Neue Encryption Exception werfen, stimmt zwar nicht ganz aber reicht aus
			throw new EncryptionErrorException();
		}

	}

	private Message encryptMessageContent(Message m) throws EncryptionErrorException {
/*
		String inhalt = m.getMessage();

		try {

			//get recievers/own private Key
			PrivateKey privateKey = getPrivateKey();
			//get senders public key
			PublicKey publicKey = getPublicKey(m.getRecieverID());

			   RSAHelper helper = new RSAHelper();
		       byte[] data = inhalt.getBytes("UTF-8");
				// Sign Message Content with own private Key
		       byte[] digitalSignature = helper.signData(data, privateKey);       
		       String signatur = new String(Base64.encode(digitalSignature, Base64.DEFAULT));
				// Encrypt Message Content with public Key
		       String encryptedText = helper.encrypt(inhalt, publicKey);
		       
			
			m.setMessage(encryptedText);
			m.setSignature(signatur);

		} catch(ContactNotExistException
			   | ConnectionFailedException
			   | UnsupportedEncodingException 
			   | InvalidKeyException 
			   | NoSuchAlgorithmException 
			   | NoSuchPaddingException 
			   | IllegalBlockSizeException 
			   | BadPaddingException 
			   | SignatureException e) {
			throw new EncryptionErrorException("Cannot retrieve Key!");
		}
*/
		return m;
	}

	private Message decryptMessageContent(Message m) throws EncryptionErrorException {
/*
		String inhalt = m.getMessage();

		try {

			//get recievers/own private Key
			PrivateKey privateKey = getPrivateKey();
			//get senders public key
			PublicKey publicKey = getPublicKey(m.getSenderID());

			//TODO Check Signed Content
			//TODO Decrypt Message Content with public Key
			   RSAHelper helper = new RSAHelper();
			 String klartext = helper.decrypt(inhalt, privateKey); 

		       byte[] ByteSignature = Base64.decode(m.getSignature(), Base64.DEFAULT);
		       
		       //Signatur echt?
		       boolean echt = helper.verifySig(klartext.getBytes("UTF-8"), publicKey, ByteSignature);
			
		       if(echt) {
					m.setMessage(klartext);
		       } else {
		    	   throw new EncryptionErrorException("Signature cannot be verified! Message content may be modified!");
		       }

		} catch(ContactNotExistException
				| ConnectionFailedException 
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| IllegalBlockSizeException 
				| BadPaddingException 
				| SignatureException 
				| UnsupportedEncodingException e) {
			throw new EncryptionErrorException("Cannot retrieve Key!");
		}
*/
		return m;
	}
	
	private PrivateKey getPrivateKey() throws ContactNotExistException, ConnectionFailedException {
		if (own_privateKey == null) {
			own_privateKey = (PrivateKey) Self.getUserFromFile(context).getPrivateKey();
		}
		return own_privateKey;
	}
	
	private PublicKey getPublicKey(String senderID)  throws ConnectionFailedException, ContactNotExistException {
		if (this.sender_publicKey == null) {
			this.sender_publicKey = (PublicKey) new ServerConnector(context).getContact(senderID).getPublicKey();
		}
		return this.sender_publicKey;
	}

	public static Long getLatestSynchronizeTimestamp(Context c) {
		context = c;
		Long return_value = 0L;

		//Read Timestamp from File
		try {

			//Decrypt with Cipher
			if (cipher_dec == null) {
				cipher_dec = Cipher.getInstance("AES");
				cipher_dec.init(Cipher.DECRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
			}

			FileInputStream fis = context.openFileInput(filename_last_sync);
			BufferedInputStream bis = new BufferedInputStream(fis);
			CipherInputStream cis = new CipherInputStream(bis, cipher_dec);

			InputStreamReader isr = new InputStreamReader(cis);
			char[] inputBuffer = new char[fis.available()];
			isr.read(inputBuffer);
			String data = new String(inputBuffer);
			isr.close();
			fis.close();

			/*
			 * converting the String data to XML format
			 * so that the DOM parser understand it as an XML input.
			 */
			InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(is);
			Element root = dom.getDocumentElement();

			//Timestamp lesen und in Returnvariable schreiben
			return_value = Long.parseLong(root.getAttribute("timestamp"));

		}
		catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException  
				| SAXException 
				| ParserConfigurationException
				| ConnectionFailedException e) {
			// TODO Auto-generated catch block -> do nothing
			e.printStackTrace();
		}

		return return_value;
	}
	public static void setLatestSynchronizeTimestamp(Context c, Long timestamp) throws ConnectionFailedException{
		context = c;
		//Write Timestamp into File

		//Try File Output
		try {

			//Encrypt with Cipher
			if (cipher_enc == null) {
				cipher_enc = Cipher.getInstance("AES");
				cipher_enc.init(Cipher.ENCRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
			}

			FileOutputStream fos = context.openFileOutput(filename_last_sync, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			CipherOutputStream cos = new CipherOutputStream(bos, cipher_enc);

			XmlSerializer serializer = Xml.newSerializer();

			serializer.setOutput(cos, "UTF-8");
			serializer.startDocument(null, Boolean.valueOf(true));
			serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			serializer.startTag(null, "sync");
			serializer.attribute(null, "timestamp", timestamp+"");
			serializer.endTag(null, "sync");
			serializer.endDocument();
			serializer.flush();
			cos.close();

		} catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException e) {
			// TODO Auto-generated catch block -> do nothing
			e.printStackTrace();
		}

	}
}