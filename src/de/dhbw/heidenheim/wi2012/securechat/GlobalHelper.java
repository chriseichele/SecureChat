package de.dhbw.heidenheim.wi2012.securechat;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

	public static void DeleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles())
				DeleteRecursive(child);

		fileOrDirectory.delete();
	}

	public static String hash(String input) throws NoSuchAlgorithmException {
	        MessageDigest digest = MessageDigest.getInstance("SHA-1");
	        digest.update(input.getBytes());
	        return new BigInteger(1, digest.digest()).toString(16);
	}

}
