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
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
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

import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactExistException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Context;
import android.util.Xml;

public class Contact {
	
	private String name;
	private String id;
	private String public_key;
	private static Context context;
	private static String filename = "contactlist.xml";

	private static Cipher cipher_enc;
	private static Cipher cipher_dec;
	
	private static ArrayList<Contact> contactList = new ArrayList<Contact>();

	public Contact(String id, String name) throws ContactNotExistException {
		this.name = name;
		this.id = id;
		getContactDetails(id);
	}
	public Contact(String id) throws ContactNotExistException {
		this.id = id;
		getContactDetails(id);
	}

	public void getContactDetails(String id) throws ContactNotExistException {
		//fehlende Kontact Details zur ID aus XML oder vom Server holen
		getContactDetailsFromServer(id);
		getContactDetailsFromXML(id);
	}
	private void getContactDetailsFromServer(String id) throws ContactNotExistException {
		// TODO Get ContactDetails from Server instead of Dummy Values
		// public key
		// public name
		
		if (id.equals("0")) {
			this.name = "Testkontakt";
		}
		else if (id.equals("1")) {
			this.name = "Chris";
		}
		else if (id.equals("2")) {
			this.name = "Dennis";
		}
		else if (id.equals("3")) {
			this.name = "Flo";
		}
		else if (id.equals("4")) {
			this.name = "Martin";
		}
		else if (id.equals("5")) {
			this.name = "Schnulf";
		}
		else if (id.equals("6")) {
			this.name = "Vera";
		}
		else {
			//Kontakt nicht gefunden
			throw new ContactNotExistException();
		}
	}
	private void getContactDetailsFromXML(String id) {
		
	}
	
	public static void setContext(Context c) {
		context = c;
	}

	public String getName() {
		return this.name;
	}
	public String getID() {
		return this.id;
	}
	public String getPublicKey() {
		return this.public_key;
	}
	
	public void addToContactList() throws ContactExistException {
		contactList.add(this);
		saveContactDetails();
	}
	
	private void saveContactDetails() throws ContactExistException {
		saveContactDetailsToXML();
		saveContactDetailsToServer();
	}
	private void saveContactDetailsToXML() throws ContactExistException {
		//Variablen initialisieren
		
		//Check if file for Contact List exists
		if(!context.getFileStreamPath(filename).exists()) {

			//Try File Creation
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
		        serializer.startTag(null, "root");
		        serializer.startTag(null, "contact");
		        serializer.attribute(null, "id", this.id);
		        serializer.attribute(null, "name", this.name);
		        serializer.endTag(null, "contact");
		        serializer.endTag(null, "root");
			    serializer.endDocument();
			    serializer.flush();
			    cos.close();
			    
			} catch (IOException
					| InvalidKeyException 
					| NoSuchAlgorithmException 
					| NoSuchPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//Kontaktlistendatei existiert
		else {
			//pruefen ob Kontakt bereits in Datei steht
			
			//neuen Kontakt in Datei einfuegen

			//Try File Output
			try {
				
				//Encrypt with Cipher
				if (cipher_enc == null) {
					cipher_enc = Cipher.getInstance("AES");
					cipher_enc.init(Cipher.ENCRYPT_MODE, ServerConnector.getFileEncryptionKey());
				}
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
			    
			    NodeList contact_nodes = root.getChildNodes();
			    for (int i=1;i<(contact_nodes.getLength()-1);i=i+2) {
			    	if (((Element) contact_nodes.item(i)).getAttribute("id").equals(this.id)) {
			    		throw new ContactExistException("Contact already exists in XML file!");
			    	}
			    }
			    // Wenn Kontakt nicht exisitert -> in XML schreiben
	            Element contact_node = dom.createElement("contact");
	            contact_node.setAttribute("id", this.id);
	            contact_node.setAttribute("name", this.name);
	            root.appendChild(contact_node);

	            Transformer transformer = TransformerFactory.newInstance().newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            // initialize StreamResult with File object to save to file
				
			    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
		        BufferedOutputStream bos = new BufferedOutputStream(fos);
		        CipherOutputStream cos = new CipherOutputStream(bos, cipher_enc);

	            StreamResult result = new StreamResult(cos);
	            DOMSource source = new DOMSource(dom);
	            transformer.transform(source, result);
	            cos.close();
				
			}
			catch (TransformerException
					| InvalidKeyException 
					| NoSuchAlgorithmException 
					| NoSuchPaddingException 
					| IOException 
					| SAXException 
					| ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void saveContactDetailsToServer() {
		
	}
	
	public static ArrayList<Contact> getContacts(Context context) {
		//Sicherstellen dass AppContext fuer Dateispeicherort gesetzt ist
		setContext(context);
		// ContactListe aus XML oder vom Server holen
        readContacts();
		
		return contactList;
	}
	private static void readContacts() {
		//Kontaktliste initialisieren
		contactList.clear();
		//read contacts from XML Contact List
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
		    
		    NodeList contact_nodes = root.getChildNodes();
		    for (int i=1;i<(contact_nodes.getLength()-1);i=i+2) {
		        //Neue Nachricht an Array ausgeben
		        contactList.add(new Contact(((Element) contact_nodes.item(i)).getAttribute("id")));
		    }
	    
		} catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| SAXException 
				| ParserConfigurationException 
				| ContactNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String getContactName(String id) {
		//XML Kontaktliste bereits eingelesen?
		if(contactList.isEmpty()) {
			//Kontakte einlesen
			readContacts();
		}
		
		//Kontakt Array nach aktueller ID durchsuchen
		for(Contact c: contactList) {
			if(c.getID().equals(id)) {
				return c.getName();
			}
		}
		//Kein Kontakt wurde gefunden
		return "";
	}
}