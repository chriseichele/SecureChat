package de.dhbw.heidenheim.wi2012.securechat.gui;

import de.dhbw.heidenheim.wi2012.securechat.GlobalHelper;
import de.dhbw.heidenheim.wi2012.securechat.R;
import de.dhbw.heidenheim.wi2012.securechat.Self;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ShowProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_profile);
		
		Intent intent = getIntent();
		if (intent != null) {
			Bundle daten = intent.getExtras();
			if(daten != null) {
				String error_message = daten.getString("error_message");
				if(error_message != null) {
					((TextView) this.findViewById(R.id.error_message)).setText(error_message);
				}
				if(daten.getBoolean("show_button_proceed")) {
					//Weiter Button anzeigen
					findViewById(R.id.button_proceed).setVisibility(View.VISIBLE);
				}
			}
		}
		
		try {
			//Userdaten aus XML Datei holen
			Self user = Self.getUserFromFile(getApplicationContext());
		
			//Userdaten auf Profilseite anzeigen
			((TextView) this.findViewById(R.id.show_id)).setText(user.getID());
			((TextView) this.findViewById(R.id.show_username)).setText(user.getName());
		} catch (ContactNotExistException e) {
			//Sollte nicht eintreten!
			GlobalHelper.displayToast_ContactNotExist(getApplicationContext());
			finish();
		} catch (ConnectionFailedException e) {
			GlobalHelper.displayToast_ConnectionFailed(getApplicationContext());
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
	        case R.id.action_logout:
	            // Popup to confirm Logout
	        	LogoutPopup();
		        //Menuauswahl erfolgreich
	            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	private void LogoutPopup() {
    	//Popup Warnmeldung
    	new AlertDialog.Builder(this)
    	.setTitle(getResources().getString(R.string.action_logout))
    	.setMessage(getResources().getString(R.string.message_logout))
    	.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    		//On Click YES
    	    public void onClick(DialogInterface dialog, int whichButton) {
    	    	DoLogout();
    	    }})
    	 .setNegativeButton(android.R.string.no, null).show();
	}
	
	private void DoLogout() {
    	//Dateien Loeschen
    	GlobalHelper.DeleteRecursive(getApplicationContext().getFilesDir());
    	//Exit to start activity
    	Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        //Activity beenden, um nicht mehr zurueckkehren zu koennen
        finish();
	}

    /** Called when the user clicks the Continue button */
	public void continueToContactList(View view) {
		//Kontaktliste aufrufen
    	Intent intent = new Intent(this,ContactListActivity.class);
        startActivity(intent);
        //Ansicht mit Button schliessen
        finish();
	}
}
