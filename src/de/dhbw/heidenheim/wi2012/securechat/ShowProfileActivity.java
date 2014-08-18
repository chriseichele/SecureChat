package de.dhbw.heidenheim.wi2012.securechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
			}
		}
		
		try {
			//Userdaten aus XML Datei holen
			User user = User.getUserFromFile(getApplicationContext());
		
			//Userdaten auf Profilseite anzeigen
			((TextView) this.findViewById(R.id.show_id)).setText(user.getID());
			((TextView) this.findViewById(R.id.show_username)).setText(user.getName());
		} catch (ContactNotExistException e) {
			//Sollte nicht eintreten!
			((TextView) this.findViewById(R.id.error_message)).setText("Unerwarteter Fehler beim abrufen der Kontaktdaten!");
		}
	}
}
