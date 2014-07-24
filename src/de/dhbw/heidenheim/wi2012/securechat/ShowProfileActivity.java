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
		
		((TextView) this.findViewById(R.id.show_id)).setText("ABCDE1234");
		((TextView) this.findViewById(R.id.show_username)).setText("Mein Benutzername");
	}
}
