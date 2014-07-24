package de.dhbw.heidenheim.wi2012.securechat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowProfileActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_profile);

		((TextView) this.findViewById(R.id.show_id)).setText("ABCDE1234");
		((TextView) this.findViewById(R.id.show_username)).setText("Mein Benutzername");
	}
}
