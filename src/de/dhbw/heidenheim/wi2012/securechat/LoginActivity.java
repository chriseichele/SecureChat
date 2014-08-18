package de.dhbw.heidenheim.wi2012.securechat;

import java.util.Locale;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends Activity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	private String userfilename;
	private Context context;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.context = getApplicationContext();
		this.userfilename = "user.xml";
		
		//Pruefen ob bereits ein Benutzer angemeldet ist
		if (checkLoggedIn()) {
			//Login Ansicht ueberspringen und direkt Kontaktliste anzeigen

	    	Intent intent = new Intent(this,ContactListActivity.class);
	        startActivity(intent);
	        //Activity beenden, um nicht mehr zurueckkehren zu koennen
	        finish();
	        
		} else {
		
		//Login oder Register View anzeigen
			
		setContentView(R.layout.activity_login);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private LoginFragment loginFragment;
		private RegisterFragment registerFragment;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			switch (position) {
			default:
			case 0:
				loginFragment = LoginFragment.newInstance(position + 1);
				return loginFragment;
			case 1:
				registerFragment = RegisterFragment.newInstance(position + 1);
				return registerFragment;
			}
		}

		public LoginFragment getLoginFragment() {
			return loginFragment;
		}
		public RegisterFragment getRegisterFragment() {
			return registerFragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}
    
    /** Called when the user clicks the Login button */
    public void userLogin(View view) {
    	//Eingaben Testen und Login

		//Benutzername
    	String user_id = (((EditText) this.findViewById(R.id.login_insert_id)).getText().toString());
    	//Passwort
    	String password = (((EditText) this.findViewById(R.id.login_insert_password)).getText().toString());
    	
    	//Eingaben leer?
    	if (user_id.trim().equals("") || password.trim().equals("")) {
    		//Leer? -> Fehlermeldung
    		mSectionsPagerAdapter.getLoginFragment().displayErrorMessage(getString(R.string.message_empty_login));
      	}
    	else {
    		//Richtige Account Details?
    		
    		//TODO Passwort hashen
    		String password_hash = password;

	        //Login
        	try {
				doLogin(user_id, password_hash);
				
		    	//Nachricht mit uebergeben
		    	Bundle daten = new Bundle();
		    	daten.putString("error_message", getString(R.string.message_login_success));
		    	//Chatliste in Hintergrund oeffnen
		    	Intent intent1 = new Intent(this,ContactListActivity.class);
		        startActivity(intent1);
		        //Profil anzeigen
		    	Intent intent2 = new Intent(this,ShowProfileActivity.class);
		    	intent2.putExtras(daten);
		        startActivity(intent2);
		        //Activity beenden, um nicht mehr zurueckkehren zu koennen
		        finish();
        	} catch (ContactNotExistException e) {
				mSectionsPagerAdapter.getLoginFragment().displayErrorMessage(getString(R.string.message_wrong_login));
        	}
        }
    }
    
    /** Called when the user clicks the Register button */
    public void userRegister(View view) {
    	//Eingaben testen und Registrieren

		//Benutzername
    	String user = (((EditText) this.findViewById(R.id.register_insert_username)).getText().toString());
    	//Passwort
    	String password1 = (((EditText) this.findViewById(R.id.register_insert_password1)).getText().toString());
		String password2 = ((EditText) this.findViewById(R.id.register_insert_password2)).getText().toString();
		
		//Eingaben leer?
		if (user.trim().equals("") || password1.trim().equals("") || password2.trim().equals("")) {
			//Leer? -> Fehlermeldung
			mSectionsPagerAdapter.getRegisterFragment().displayErrorMessage(getString(R.string.message_empty_register));
    	}
        //Gewaehltes Passwort identitsch?
		else if(!password1.equals(password2)) {
        	//Nein? -> Fehlermeldung
			mSectionsPagerAdapter.getRegisterFragment().displayErrorMessage(getString(R.string.message_password_not_identical));
        } else {
        	//TODO Passwort hashen
    		String password_hash = password1;
    		
			//Registrieren
        	doRegister(user, password_hash);
			
	    	//Nachricht mit uebergeben
	    	Bundle daten = new Bundle();
	    	daten.putString("error_message", getString(R.string.message_registered_success));
	    	//Chatliste in Hintergrund oeffnen
	    	Intent intent1 = new Intent(this,ContactListActivity.class);
	        startActivity(intent1);
	        //Profil anzeigen
	    	Intent intent2 = new Intent(this,ShowProfileActivity.class);
	    	intent2.putExtras(daten);
	        startActivity(intent2);
	        //Activity beenden, um nicht mehr zurueckkehren zu koennen
	        finish();
        }
    }
    
    private void doRegister(String username, String password_hash) {
    	//User neu am Server registrieren und Daten holen
    	User user = doRegisterOnServer(username, password_hash);
    	//Userdaten lokal speichern
    	user.saveToXML(getApplicationContext());
    }
    
    private User doRegisterOnServer(String username, String password_hash) {
    	//TODO return user ID from Server
    	String id = "ASDF1234";
    	//TODO return private Key from Server
    	String private_key = "1234";
    	
    	return new User(id, username, private_key);
    }
    
    private void doLogin(String userID, String password_hash) throws ContactNotExistException {
    	//Userdaten vom Server holen
    	User user = doLoginOnServer(userID, password_hash);
    	//Userdaten lokal ablegen
    	user.saveToXML(getApplicationContext());
    }
    
    private User doLoginOnServer(String userID, String password_hash) throws ContactNotExistException {
    	//TODO Contact mit Server ueberpruefen und Kontaktdaten holen
    	String username = "Dummy";
    	String private_key = "1234";
    	//TODO Exception, wenn nicht existent
    	
    	if (userID.equals("admin") && password_hash.equals("password")) {
    		//User Objekt anlegen und zurueckgeben
        	return new User(userID, username, private_key);
		} else {
			throw new ContactNotExistException();
		}
    }
    
    private boolean checkLoggedIn() {
    	//Ist ein Benutzer angemeldet?
    	try {
    		User.getUserFromFile(getApplicationContext());
    		//Wenn noch kein Abbruch, dann erfolgreich
    		return true;
    	} catch (ContactNotExistException e) {
    		//Ansonsten nicht angemeldet
    		return false;
    	}
    }

}
