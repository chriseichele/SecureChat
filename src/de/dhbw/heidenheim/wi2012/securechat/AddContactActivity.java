package de.dhbw.heidenheim.wi2012.securechat;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class AddContactActivity extends Activity {

	private EditText textfield;
	private TextView error_message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);

		//Eingabefeld fuer neue Textnachricht holen
		textfield = (EditText) findViewById(R.id.insert_id);
		//Textview fuer Fehlermeldungen holen
		error_message = (TextView) findViewById(R.id.error_message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		//int id = item.getItemId();
		
		return super.onOptionsItemSelected(item);
	}

    /** Called when the user clicks the AddContact button */
    public void addContact(View view) {
    	// Kontakt hinzufuegen
    	
    	// KontaktID aus Textfeld holen
    	String newContact = textfield.getText().toString().trim(); 
		if(newContact.length() > 0)
		{
			//Eingabefeld leeren
			textfield.setText("");
			try {
			// Neuen Kontakt anlegen
	    	Contact c = new Contact(newContact);
	    	//App context in Kontakt speichern
	    	Contact.setContext(getApplicationContext());
		    	//Kontakt in Kontaktliste speichern
		    	try {
					c.addToContactList();
					//Kontaktliste ueber aenderung benachrichtigen
					ContactListAdapter.cla.notifyDataSetChanged();
			    	//Nachricht Kontakt "Name" erfolgreich hinzugefuegt
		    		error_message.setTextColor(getResources().getColor(R.color.android_green_dark));
		    		error_message.setText(getString(R.string.message_contact_added, c.getName()));
				} catch (ContactExistException e) {
		    		//Kontakt existiert bereits!
		    		//Fehlermeldung zeigen
		    		error_message.setTextColor(getResources().getColor(R.color.android_red_dark));
		    		error_message.setText(getString(R.string.message_contact_already_in_list, newContact));
				}
			} catch (ContactNotExistException e) {
	    		//Kontakt existiert nicht!
	    		//Fehlermeldung zeigen
	    		error_message.setTextColor(getResources().getColor(R.color.android_red_dark));
	    		error_message.setText(getString(R.string.message_contact_not_added, newContact));
	    	}
		}
    }
}
