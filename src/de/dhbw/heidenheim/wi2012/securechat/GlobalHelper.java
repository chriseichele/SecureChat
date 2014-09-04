package de.dhbw.heidenheim.wi2012.securechat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;

public class GlobalHelper {

	private static void displayToast(Context c, String s) {
		Toast.makeText(c, s, Toast.LENGTH_LONG).show();
	}

	public static void displayToast_ConnectionFailed(Context c) {
		displayToast(c, c.getString(R.string.message_connection_failed));
	}
	public static void displayToast_EncryptionError(Context c) {
		displayToast(c, c.getString(R.string.message_encryption_error));
	}
	public static void displayToast_ContactNotExist(Context c) {
		displayToast(c, c.getString(R.string.message_contact_not_exist));
	}
	public static void displayToast_newMessage(Context c, int anz, String sender_id) {
		try {
			if(anz == 1) {
				displayToast(c, c.getString(R.string.message_newMessage_from, Contact.getContactName(sender_id)));
			} else {
				displayToast(c, c.getString(R.string.message_newMessages_from, Contact.getContactName(sender_id), anz));
			}
		} catch (ContactNotExistException | ConnectionFailedException e) {
			if(anz == 1) {
				displayToast(c, c.getString(R.string.message_newMessage_from_unknown, sender_id));
			} else {
				displayToast(c, c.getString(R.string.message_newMessages_from_unknown, sender_id, anz));
			}
		}
		playNotificationSound(c);
	}
	private static void playNotificationSound(Context c) {
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone r = RingtoneManager.getRingtone(c, notification);
			r.play();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PrivateKey getRSAprivateKey(String pks) throws NoSuchAlgorithmException, InvalidKeySpecException {

		byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		// get the private key
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		KeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;

		//byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		//return new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA"); 
	}
	public static PublicKey getRSApublicKey(String pks) throws NoSuchAlgorithmException, InvalidKeySpecException {

		byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		// get the public key
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		KeySpec publicKeySpec = new PKCS8EncodedKeySpec(encodedKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;

		//byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		//return new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA"); 
	}
	public static String getRSAString(Key key) {
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}

	public static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}

	public static String hash(String input, String keyString) throws UnsupportedEncodingException, 
	NoSuchAlgorithmException, 
	InvalidKeyException {
		//Hash Password with HMAC
		SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);

		byte[] bytes = mac.doFinal(input.getBytes("UTF-8"));

		return new String( Base64.encode(bytes, 0) );
	}

}
