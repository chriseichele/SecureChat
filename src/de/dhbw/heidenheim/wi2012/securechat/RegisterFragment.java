package de.dhbw.heidenheim.wi2012.securechat;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class RegisterFragment extends Fragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	private static final String ARG_SECTION_NUMBER = "section_number";
	private View rootView;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static RegisterFragment newInstance(int sectionNumber) {
		RegisterFragment fragment = new RegisterFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}

	public RegisterFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.fragment_register, container, false);
		return rootView;
	}
	
	public void displayErrorMessage(String text) {
		//Fehlermeldung anzeigen, wenn nicht leer
		if(text != null) {
			((TextView) this.rootView.findViewById(R.id.register_error_message)).setText(text);
		}
	}
}
