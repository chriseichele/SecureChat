package de.dhbw.heidenheim.wi2012.securechat.gui;

import de.dhbw.heidenheim.wi2012.securechat.Contact;
import de.dhbw.heidenheim.wi2012.securechat.GlobalHelper;
import de.dhbw.heidenheim.wi2012.securechat.MessageUpdater;
import de.dhbw.heidenheim.wi2012.securechat.R;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ConnectionFailedException;
import de.dhbw.heidenheim.wi2012.securechat.exceptions.ContactNotExistException;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;

/**
 * An activity representing a single Contact detail screen. This activity is
 * only used on handset devices. On tablet-size devices, item details are
 * presented side-by-side with a list of items in a {@link ContactListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link ChatDetailFragment}.
 */
public class ChatDetailActivity extends Activity {
	
	private ChatDetailFragment detailFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_detail);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ChatDetailFragment.CHAT_OPPONENT, 
					getIntent().getStringExtra(ChatDetailFragment.CHAT_OPPONENT));
			this.detailFragment = new ChatDetailFragment();
			this.detailFragment.setArguments(arguments);
			getFragmentManager().beginTransaction().add(R.id.chat_detail_container, this.detailFragment).commit();
		}
		
		//Chatparter Name als Titel
		try {
			this.setTitle(Contact.getContactName(getIntent().getStringExtra(ChatDetailFragment.CHAT_OPPONENT)));
		} catch (ConnectionFailedException e) {
    		//Toast Message mit Fehlermeldung zeigen
    		GlobalHelper.displayToast_ConnectionFailed(getApplicationContext());
    		//Als Fallback ID in Titel schreiben, damit er nicht leer bleibt
			this.setTitle(getIntent().getStringExtra(ChatDetailFragment.CHAT_OPPONENT));
    	} catch (ContactNotExistException e) {
    		//Should not happen
    		
    		//Als Fallback ID in Titel schreiben, damit er nicht leer bleibt
			this.setTitle(getIntent().getStringExtra(ChatDetailFragment.CHAT_OPPONENT));
    	}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageUpdater messageUpdater = ContactListActivity.getMessageUpdater();
		if(messageUpdater != null) {
			messageUpdater.stopUpdates();
		}
		//Set Detail Fragment to enable Message Updater to Notify Fragment of new Messages
		MessageUpdater.setChatDetailFragment(null);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MessageUpdater messageUpdater = ContactListActivity.getMessageUpdater();
		if(messageUpdater != null) {
			messageUpdater.stopUpdates();
		}
		//Set Detail Fragment to enable Message Updater to Notify Fragment of new Messages
		MessageUpdater.setChatDetailFragment(null);
	}
	@Override
	protected void onResume() {
		super.onResume();
		MessageUpdater messageUpdater = ContactListActivity.getMessageUpdater();
		if(messageUpdater != null) {
			messageUpdater.startUpdates();
		}
		//Set Detail Fragment to enable Message Updater to Notify Fragment of new Messages
		MessageUpdater.setChatDetailFragment(detailFragment);
	}
	@Override
	protected void onStart() {
		super.onStart();
		MessageUpdater messageUpdater = ContactListActivity.getMessageUpdater();
		if(messageUpdater != null) {
			messageUpdater.startUpdates();
		}
		//Set Detail Fragment to enable Message Updater to Notify Fragment of new Messages
		MessageUpdater.setChatDetailFragment(detailFragment);
	}
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
		this.detailFragment.sendMessage(view);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			navigateUpTo(new Intent(this, ContactListActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
