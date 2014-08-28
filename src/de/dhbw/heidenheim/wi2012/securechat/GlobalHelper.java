package de.dhbw.heidenheim.wi2012.securechat;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
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
	
	public static Key getRSAKey(String pks) {
		byte[] encodedKey = Base64.decode(pks, Base64.DEFAULT);
		return new SecretKeySpec(encodedKey,0,encodedKey.length, "RSA"); 
	}

}
