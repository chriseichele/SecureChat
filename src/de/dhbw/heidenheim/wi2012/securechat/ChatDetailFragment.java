package de.dhbw.heidenheim.wi2012.securechat;

import java.util.ArrayList;

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
	ChatHistory chatHistory;
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String CHAT_OPPONENT = "ID of Chat Partner";
	private String chat_opponent;

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
			this.chat_opponent = getArguments().getString(CHAT_OPPONENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_chat,
				container, false);
		
		//Eingabefeld fuer neue Textnachricht holen
		textfield = (EditText) rootView.findViewById(R.id.edit_message);
		
		if(chat_opponent == null) {
			this.chat_opponent = getActivity().getIntent().getStringExtra(CHAT_OPPONENT);
		}
 	    
		//Nachrichten holen
		chatHistory = new ChatHistory(chat_opponent, getActivity().getApplicationContext());
		messages = chatHistory.getCurrentMessages();

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
		chatHistory.add(m);
		adapter.notifyDataSetChanged();
		chatList.setSelection(messages.size()-1);
	}
}
