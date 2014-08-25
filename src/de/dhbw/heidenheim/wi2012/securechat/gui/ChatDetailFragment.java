package de.dhbw.heidenheim.wi2012.securechat.gui;

import java.util.ArrayList;

import de.dhbw.heidenheim.wi2012.securechat.ChatHistory;
import de.dhbw.heidenheim.wi2012.securechat.Message;
import de.dhbw.heidenheim.wi2012.securechat.R;
import de.dhbw.heidenheim.wi2012.securechat.Self;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;
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
	private String chat_opponent_id;
	private String my_user_id;

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
			this.chat_opponent_id = getArguments().getString(CHAT_OPPONENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_chat, container,
				false);

		// getCurrentUserID
		try {
			my_user_id = Self.getUserFromFile(
					getActivity().getApplicationContext()).getID();

			// Eingabefeld fuer neue Textnachricht holen
			textfield = (EditText) rootView.findViewById(R.id.edit_message);

			if (chat_opponent_id == null) {
				this.chat_opponent_id = getActivity().getIntent()
						.getStringExtra(CHAT_OPPONENT);
			}

			// Nachrichten holen
			chatHistory = new ChatHistory(chat_opponent_id, getActivity()
					.getApplicationContext());
			messages = chatHistory.getCurrentMessages();

			adapter = new ChatListAdapter(getActivity(), messages);
			chatList = (ListView) rootView.findViewById(R.id.chatList);
			chatList.setAdapter(adapter);

			// Zum unteren Ende der Anzeige Springen
			scrollChatViewToBottom();

		} catch (ContactNotExistException e) {
			// Fall Sollte eigentlich nicht auftreten
			// In Kontaktliste Ansicht, aber kein User eingeloggt
			// Zurueck zur Login Seite
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			startActivity(intent);
			getActivity().finish();
		}

		return rootView;
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		// Do something in response to button
		String newMessage = textfield.getText().toString().trim();
		if (newMessage.length() > 0) {
			textfield.setText("");
			sendNewMessage(new Message(newMessage, true, my_user_id, chat_opponent_id));
		}

		// TODO Dummy Antwort nach jeder neuen Nachricht entfernen
		chatHistory.add(new Message("Und was kann die App bis jetzt schon alles?", false, chat_opponent_id, my_user_id));
		adapter.notifyDataSetChanged();
		chatList.setSelection(messages.size() - 1);
	}

	private void sendNewMessage(Message m) {
		chatHistory.send(m);
		adapter.notifyDataSetChanged();
		chatList.setSelection(messages.size() - 1);
	}

	private void scrollChatViewToBottom() {
		// Zum unteren Ende der Anzeige scrollen
		chatList.postDelayed(new Runnable() {
			@Override
			public void run() {
				chatList.smoothScrollToPosition(chatList.getCount());
			}
		}, 100);
	}
}
