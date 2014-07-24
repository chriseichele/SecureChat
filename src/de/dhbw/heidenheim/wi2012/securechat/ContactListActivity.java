package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContactListActivity extends Activity implements OnItemClickListener {
	
    public final static String CHAT_OPPONENT = "";
	
	private ListView contactListView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
         //Activity Layout Setzen
	     setContentView(R.layout.activity_contact_list);

         //List View Element fuer Kontaktliste finden
	     contactListView = (ListView) findViewById(R.id.contactList);

         // Instanciating an array list with data
         List<String> contactList = new ArrayList<String>();
         contactList.add("Schnulf");
         contactList.add("Dennis");
         contactList.add("Martin");
         contactList.add("Flo");
         contactList.add("Vera");
         contactList.add("Chris");

         // This is the array adapter, it takes the context of the activity as a 
         // first parameter, the type of list view as a second parameter and your 
         // array as a third parameter.
         ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                 this, 
                 android.R.layout.simple_list_item_1,
                 contactList );

         contactListView.setAdapter(arrayAdapter); 
         
         contactListView.setOnItemClickListener(this); 
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Intent intent;
        switch (item.getItemId()) {
	        case R.id.action_showProfile:
	            //AddContact
	        	intent = new Intent(this, ShowProfileActivity.class);
	            startActivity(intent);
	            return true;
	        case R.id.action_addContact:
	            //AddContact
	        	intent = new Intent(this, AddContactActivity.class);
	            startActivity(intent);
	            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        //startChat
    	Intent intent = new Intent(this, ChatActivity.class);
    	intent.putExtra(CHAT_OPPONENT, ((TextView) view).getText());	
        startActivity(intent);
	}
}
