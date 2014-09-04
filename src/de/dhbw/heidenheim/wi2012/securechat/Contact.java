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
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactExistException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Context;
import android.util.Xml;

public class Contact {
	
	private String name;
	private String id;
	private PublicKey public_key;
	private static Context context;
	private static String filename = "contactlist.xml";

	private static Cipher cipher_enc;
	private static Cipher cipher_dec;
	
	private static ArrayList<Contact> contactList = new ArrayList<Contact>();

	public Contact(String id, String name, PublicKey public_key) {
		this.id = id;
		this.name = name;
		this.public_key = public_key;
	}
	public Contact(String id) throws ContactNotExistException, ConnectionFailedException {
		this.id = id;
		getContactDetailsFromServer(id);
	}

	private void getContactDetailsFromServer(String id) throws ContactNotExistException, ConnectionFailedException {
		// Get ContactDetails from Server
		Contact c = new ServerConnector(context).getContact(id);
		
		// Write Contact Details in Object Variables
		// public name
		this.name = c.getName();
		// public key
		this.public_key = c.getPublicKey();
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
	public PublicKey getPublicKey() {
		return this.public_key;
	}
	
	public void addToContactList() throws ContactExistException, ConnectionFailedException {
		contactList.add(this);
		saveContactDetailsToXML();
	}
	
	private void saveContactDetailsToXML() throws ContactExistException, ConnectionFailedException {
		//Variablen initialisieren
		
		//Check if file for Contact List exists
		if(!context.getFileStreamPath(filename).exists()) {

			//Try File Creation
			try {
				
				//Encrypt with Cipher
				if (cipher_enc == null) {
					cipher_enc = Cipher.getInstance("AES");
					cipher_enc.init(Cipher.ENCRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
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
		        serializer.attribute(null, "publicKey", RSAHelper.getPublicKeyString(this.public_key));
		        serializer.endTag(null, "contact");
		        serializer.endTag(null, "root");
			    serializer.endDocument();
			    serializer.flush();
			    cos.close();
			    
			} catch (IOException
					| InvalidKeyException 
					| NoSuchAlgorithmException 
					| NoSuchPaddingException 
					| IllegalArgumentException 
					| IllegalStateException e) {
				// TODO Auto-generated catch block -> do nothing
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
					cipher_enc.init(Cipher.ENCRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
				}
				//Decrypt with Cipher
				if (cipher_dec == null) {
					cipher_dec = Cipher.getInstance("AES");
					cipher_dec.init(Cipher.DECRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
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
	            contact_node.setAttribute("publicKey", RSAHelper.getPublicKeyString(this.public_key));
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
					| ParserConfigurationException 
					| DOMException e) {
				// TODO Auto-generated catch block -> do nothing
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList<Contact> getContacts(Context context) throws ConnectionFailedException {
		//Sicherstellen dass AppContext fuer Dateispeicherort gesetzt ist
		setContext(context);
		// ContactListe aus XML holen
        readContacts();
		
		return contactList;
	}
	private static void readContacts() throws ConnectionFailedException {
		//Kontaktliste initialisieren
		contactList.clear();
		//read contacts new from XML Contact List
		try {
			
			//Decrypt with Cipher
			if (cipher_dec == null) {
				cipher_dec = Cipher.getInstance("AES");
				cipher_dec.init(Cipher.DECRYPT_MODE, new ServerConnector(context).getFileEncryptionKey());
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
		    	PublicKey key = RSAHelper.getPublicKey(((Element) contact_nodes.item(i)).getAttribute("publicKey"));
		        //Neuen Kontakt an Array geben
		        contactList.add(new Contact(((Element) contact_nodes.item(i)).getAttribute("id"),
											((Element) contact_nodes.item(i)).getAttribute("name"),
											key));
		    }
	    
		} catch (IOException
				| InvalidKeyException 
				| NoSuchAlgorithmException 
				| NoSuchPaddingException 
				| SAXException 
				| ParserConfigurationException 
				| InvalidKeySpecException e) {
			// Auto-generated catch block
			e.printStackTrace();
			// do Nothing, Contact List will just stay empty
		}
	}
	public static String getContactName(String id) throws ConnectionFailedException, ContactNotExistException {
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
		throw new ContactNotExistException("Contact not found!");
		//return "";
	}
}