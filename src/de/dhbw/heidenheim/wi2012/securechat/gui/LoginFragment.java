package de.dhbw.heidenheim.wi2012.securechat.gui;

import de.dhbw.heidenheim.wi2012.securechat.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private View rootView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static LoginFragment newInstance(int sectionNumber) {
		LoginFragment fragment = new LoginFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public LoginFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_login, container, false);
		return rootView;
	}
	
	public void displayErrorMessage(String text) {
		//Fehlermeldung anzeigen, wenn nicht leer
		if(text != null) {
			((TextView) this.rootView.findViewById(R.id.login_error_message)).setText(text);
		}
	}
}
