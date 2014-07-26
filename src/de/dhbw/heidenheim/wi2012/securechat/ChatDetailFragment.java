package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

/**
 * A fragment representing a single Contact detail screen. This fragment is
 * either contained in a {@link ContactListActivity} in two-pane mode (on
 * tablets) or a {@link ChatDetailActivity} on handsets.
 */
public class ChatDetailFragment extends Fragment {

	ListView chatList;
	ArrayList<Message> messages;
	ChatListAdapter adapter;
	EditText textfield;
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String CHAT_OPPONENT = "Name of Chat Partner";

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ChatDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(CHAT_OPPONENT)) {
			// In a real-world scenario, use a Loader
			// to load content from a content provider.
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_chat,
				container, false);
		
		//Textnachricht holen
		textfield = (EditText) rootView.findViewById(R.id.edit_message);
 	    
		messages = new ArrayList<Message>();

		messages.add(new Message("Hey", false, new Date(1406066828000L)));
		messages.add(new Message("Hi!", true, new Date(1406146928000L)));
		messages.add(new Message("Wie gehts??", false, new Date(1406156958000L)));
		messages.add(new Message("Ganz gut ;)", true, new Date(1406207028000L)));
		messages.add(new Message("arbeite gerade an einer sicheren Chat-App", true, new Date(1406267128000L)));
		messages.add(new Message("Wow, cool!", false, new Date(1406267338000L)));


		adapter = new ChatListAdapter(getActivity(), messages);
		chatList = (ListView) rootView.findViewById(R.id.chatList);
		chatList.setAdapter(adapter);
		
		addNewMessage(new Message("Und was kann die App bis jetzt schon alles?", false));

		return rootView;
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
