package de.dhbw.heidenheim.wi2012.securechat;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

public class GlobalHelper {

	private static void displayToast(Context c, String s) {
		Toast.makeText(c, s, Toast.LENGTH_LONG).show();
	}
	private static void displayShortToast(Context c, String s) {
		Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
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
	public static void displayToast_newMessage(Context c, int anz) {
		if(anz == 1) {
			displayShortToast(c, c.getString(R.string.message_newMessage, anz));
		} else {
			displayShortToast(c, c.getString(R.string.message_newMessages, anz));
		}
	}

	public static Key getRSAKey(String pks) {
		byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		return new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA"); 
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
