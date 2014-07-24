package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {

	ListView chatList;
	ArrayList<Message> messages;
	ChatListAdapter adapter;
	EditText textfield;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		//Textnachricht holen
		textfield = (EditText) this.findViewById(R.id.edit_message);
		
 	    // Get the ChatOpponent from the intent
 	    Intent intent = getIntent();
 	    String chat_opponent = intent.getStringExtra(ContactListActivity.CHAT_OPPONENT);
		
		this.setTitle(chat_opponent);
		messages = new ArrayList<Message>();

		messages.add(new Message("Hey", false));
		messages.add(new Message("Hi!", true));
		messages.add(new Message("Wie gehts??", false));
		messages.add(new Message("Ganz gut ;)", true));
		messages.add(new Message("arbeite gerade an einer sicheren Chat-App", true));
		messages.add(new Message("Wow, cool!", false));


		adapter = new ChatListAdapter(this, messages);
		chatList = (ListView) findViewById(R.id.chatList);
		chatList.setAdapter(adapter);
		
		addNewMessage(new Message("Und was kann die App bis jetzt schon alles?", false));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
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
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
		String newMessage = textfield.getText().toString().trim(); 
		if(newMessage.length() > 0)
		{
			textfield.setText("");
			addNewMessage(new Message(newMessage, true));
		}
    }
    

	private void addNewMessage(Message m)
	{
		messages.add(m);
		adapter.notifyDataSetChanged();
		chatList.setSelection(messages.size()-1);
	}
}
