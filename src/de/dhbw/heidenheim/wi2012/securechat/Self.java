package de.dhbw.heidenheim.wi2012.securechat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Context;
import android.util.Xml;

public class Self {
	
	private String id;
	private String name;
	private String private_key;
	
	private static Context context;
	private static String filename = "user.xml";

	private static Cipher cipher_enc;
	private static Cipher cipher_dec;

	public Self(String id, String name, String private_key) {
		this.id = id;
		this.name = name;
		this.private_key = private_key;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPrivateKey() {
		return this.private_key;
	}
	
	public void saveToXML(Context c) {
		//AppContext zwischenspeichern
		context = c;
		
    	//ID & Name & PrivateKey lokal in XML ablegen
		try {
			
			//Encrypt with Cipher
			if (cipher_enc == null) {
				cipher_enc = Cipher.getInstance("AES");
				cipher_enc.init(Cipher.ENCRYPT_MODE, ServerConnector.getFileEncryptionKey());
			}
			
		    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
	        BufferedOutputStream bos = new BufferedOutputStream(fos);
	        CipherOutputStream cos = new CipherOutputStream(bos, cipher_enc);
	    
		    XmlSerializer serializer = Xml.newSerializer();
		    	
			serializer.setOutput(cos, "UTF-8");
		    serializer.startDocument(null, Boolean.valueOf(true));
		    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	        serializer.startTag(null, "user");
	        serializer.attribute(null, "id", this.id);
	        serializer.attribute(null, "name", this.name);
	        serializer.attribute(null, "private_key", this.private_key);
	        serializer.endTag(null, "user");
		    serializer.endDocument();
		    serializer.flush();
		    cos.close();
		    
		} catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException
				| ConnectionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Self getUserFromFile(Context c) throws ContactNotExistException, ConnectionFailedException {
		//AppContext zwischenspeichern
		context = c;
		//Versuche Userdatei zu lesen
		if(context != null) {
			if(context.getFileStreamPath(filename).exists()) {
				//Userdaten aus XML Datei holen
				try {
					
					//Decrypt with Cipher
					if (cipher_dec == null) {
						cipher_dec = Cipher.getInstance("AES");
						cipher_dec.init(Cipher.DECRYPT_MODE, ServerConnector.getFileEncryptionKey());
					}
			
				    FileInputStream fis = context.openFileInput(filename);
			        BufferedInputStream bis = new BufferedInputStream(fis);
			        CipherInputStream cis = new CipherInputStream(bis, cipher_dec);
			
				    InputStreamReader isr = new InputStreamReader(cis);
				    char[] inputBuffer = new char[fis.available()];
				    isr.read(inputBuffer);
				    String data = new String(inputBuffer);
				    isr.close();
				    cis.close();
			
				    /*
				     * converting the String data to XML format
				     * so that the DOM parser understand it as an XML input.
				     */
				    InputStream is = new ByteArrayInputStream(data.getBytes("UTF-8"));
			
				    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				    DocumentBuilder db = dbf.newDocumentBuilder();
				    Document dom = db.parse(is);
				    Element root = dom.getDocumentElement();
				    
				    String id = root.getAttribute("id");
				    String username = root.getAttribute("name");
				    String private_key = root.getAttribute("private_key");
				    
					//User Objekt anlegen und zurueckgeben
					return new Self(id, username, private_key);
			    
				} catch (IOException
						| InvalidKeyException 
						| NoSuchAlgorithmException 
						| NoSuchPaddingException
						| SAXException 
						| ParserConfigurationException e) {
					//Exception bei Fehler
					throw new ContactNotExistException("Userdaten konnten nicht gelesen werden!");
				}
			} else {
				throw new ContactNotExistException("No Contact File!");
			}
		} else {
			throw new ContactNotExistException("Cannot Access Contact File!");
		}
	}

}
