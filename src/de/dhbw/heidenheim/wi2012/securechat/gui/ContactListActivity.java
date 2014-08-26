package de.dhbw.heidenheim.wi2012.securechat.gui;

import java.util.ArrayList;

import de.dhbw.heidenheim.wi2012.securechat.*;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.Activity;

/**
 * An activity representing a list of Contacts. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ChatDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ContactListFragment} and the item details (if present) is a
 * {@link ChatDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ContactListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ContactListActivity extends Activity implements
		ContactListFragment.Callbacks {

	// Contact Name
	public final static String CHAT_OPPONENT = "";

	private static final int REQUEST_EXIT = 0;

	private ChatDetailFragment detailFragment;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	private MessageUpdater messageUpdater;

	private String currentUserID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);

		// getCurrentUserID
		try {
			currentUserID = Self.getUserFromFile(getApplicationContext()).getID();

			// Starte Service um neue Nachrichten zu holen
			messageUpdater = new MessageUpdater(new Runnable() {
				@Override
				public void run() {
					//Definiere Run() Methode des Message Updaters

					Long last_sync = ChatHistory.getLatestSynchronizeTimestamp(getApplicationContext());
					
					ArrayList<Message> messages = new ServerConnector().getNewMessages(
							last_sync,
							currentUserID);

					// Nachrichten verarbeiten
					for(int i=0;i<messages.size();i++) {
						// durchgehen und je nach Sender in passende Chat History schreiben
						Message m = messages.get(i);
						ChatHistory ch = new ChatHistory(m.getSender(), getApplicationContext());
						ch.add(m);
						//Nachricht neuer? -> Timestamp aktualisieren
						if (m.getTimestamp() > last_sync) {
							last_sync = m.getTimestamp();
						}
					}
					// LatestSyncTimestamp auf Wert von letzter Nachricht setzen
					ChatHistory.setLatestSynchronizeTimestamp(getApplicationContext(), last_sync);
				}
			});

			if (findViewById(R.id.contact_detail_container) != null) {
				// The detail container view will be present only in the
				// large-screen layouts (res/values-large and
				// res/values-sw600dp). If this view is present, then the
				// activity should be in two-pane mode.
				mTwoPane = true;

				// App name als Titel zeigen
				this.setTitle(getResources().getString(R.string.app_name));

				// In two-pane mode, list items should be given the
				// 'activated' state when touched.
				((ContactListFragment) getFragmentManager().findFragmentById(
						R.id.contact_list)).setActivateOnItemClick(true);
			}

			// Versuche direkt zum Profil zu springen wenn erwuenscht
			try {
				if (getIntent().getExtras().getBoolean("start_profile_view") == true) {
					// Profil anzeigen
					Intent intent2 = new Intent(this, ShowProfileActivity.class);
					intent2.putExtras(getIntent().getExtras());
					startActivityForResult(intent2, REQUEST_EXIT);
				}
			} catch (NullPointerException e) {
				// No Extra Data
				// Just do Nothing
			}

		} catch (ContactNotExistException e) {
			// Fall Sollte eigentlich nicht auftreten
			// In Kontaktliste Ansicht, aber kein User eingeloggt
			// Zurueck zur Login Seite
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		} catch (ConnectionFailedException e) {
			GlobalHelper.displayToast_ConnectionFailed(getApplicationContext());
		}
	}

	/**
	 * Callback method from {@link ContactListFragment.Callbacks} indicating
	 * that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatDetailFragment.CHAT_OPPONENT, id);
			this.detailFragment = new ChatDetailFragment();
			this.detailFragment.setArguments(arguments);
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.contact_detail_container, this.detailFragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ChatDetailActivity.class);
			detailIntent.putExtra(ChatDetailFragment.CHAT_OPPONENT, id);
			startActivity(detailIntent);
		}
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
			// AddContact
			intent = new Intent(this, ShowProfileActivity.class);
			startActivityForResult(intent, REQUEST_EXIT);
			return true;
		case R.id.action_addContact:
			// AddContact
			intent = new Intent(this, AddContactActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_EXIT) {
			// Finishes Activity if Child is Finished
			finish();
		}
	}

	/** Called when the user clicks the Send button */
	public void sendMessage(View view) {
		if (mTwoPane) {
			this.detailFragment.sendMessage(view);
		}
	}
}
