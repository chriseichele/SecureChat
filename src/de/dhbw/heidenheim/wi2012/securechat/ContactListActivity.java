package de.dhbw.heidenheim.wi2012.securechat;

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

    //Contact Name
	public final static String CHAT_OPPONENT = "";
	
	private ChatDetailFragment detailFragment;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);

		if (findViewById(R.id.contact_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
			
			//App name als Titel zeigen
			this.setTitle(getResources().getString(R.string.app_name));

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ContactListFragment) getFragmentManager().findFragmentById(
					R.id.contact_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
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
			getFragmentManager().beginTransaction()
					.replace(R.id.contact_detail_container, this.detailFragment).commit();

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
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
		if (mTwoPane) {
			this.detailFragment.sendMessage(view);
		}
    }
}
