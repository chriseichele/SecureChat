package de.dhbw.heidenheim.wi2012.securechat.gui;

import de.dhbw.heidenheim.wi2012.securechat.R;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
		
		EditText password_field = (EditText) this.rootView.findViewById(R.id.register_insert_password1);
		
		password_field.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				// Auto-generated method stub
			}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto-generated method stub              
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	//Passwortstaerke pruefen und darstellen
            	showPasswordStrength(s.toString());
            }
            private int getPasswordStrength(String pw) {
            	//Calculate password strength value
            	 int strengthPercentage = 0;
            	 
                 String[] partialRegexChecks = { ".*[a-z]+.*", // lower
                         ".*[A-Z]+.*", 						   // upper
                         ".*[\\d]+.*", 						   // digits
                         ".*[!@#$%^&*-]+.*", 		   // symbols
                         ".{6}+.*", 						   // length >= 6
                         ".{10}+.*", 						   // length >= 10
                         ".{16}+.*" 						   // length >= 16
                 };
                 if (pw.matches(partialRegexChecks[0])) {
                     strengthPercentage += 15;
                 }
                 if (pw.matches(partialRegexChecks[1])) {
                     strengthPercentage += 15;
                 }
                 if (pw.matches(partialRegexChecks[2])) {
                     strengthPercentage += 15;
                 }
                 if (pw.matches(partialRegexChecks[3])) {
                     strengthPercentage += 15;
                 }
                 if (pw.matches(partialRegexChecks[4])) {
                     strengthPercentage += 10;
                 }
                 if (pw.matches(partialRegexChecks[5])) {
                     strengthPercentage+=15;
                 }
                 if (pw.matches(partialRegexChecks[6])) {
                     strengthPercentage += 15;
                 }

                 return strengthPercentage;
            }
            private void showPasswordStrength(String pw) {
            	//Get Password Strength Value
            	int value = getPasswordStrength(pw);
            	//Get Text View for Result
            	TextView password_strength_view = ((TextView) rootView.findViewById(R.id.register_password_strength));
            	//Result Variablen
            	String result_text = "";
            	int result_color = 0;
            	//Anzeigewerte fuer Passwortstaerke bestimmen
            	if(value <= 0) {
            		result_text = "";
            		result_color = getResources().getColor(R.color.transparent);
            	}
            	else if (value < 25) {
            		result_text = getResources().getString(R.string.password_strength_very_very_weak);
            		result_color = getResources().getColor(R.color.android_red_dark);
            	}
            	else if (value < 45) {
            		result_text = getResources().getString(R.string.password_strength_very_weak);
            		result_color = getResources().getColor(R.color.android_red);
            	}
            	else if (value < 65) {
            		result_text = getResources().getString(R.string.password_strength_weak);
            		result_color = getResources().getColor(R.color.android_orange);
            	}
            	else if (value < 85) {
            		result_text = getResources().getString(R.string.password_strength_moderate);
            		result_color = getResources().getColor(R.color.yellow);
            	}
            	else if (value < 95) {
            		result_text = getResources().getString(R.string.password_strength_strong);
            		result_color = getResources().getColor(R.color.android_green);
            	}
            	else {
            		result_text = getResources().getString(R.string.password_strength_very_strong);
            		result_color = getResources().getColor(R.color.android_green_dark);
            	}
            	//Show Password Strength
        		password_strength_view.setText(result_text);
        		password_strength_view.setBackgroundColor(result_color);
            }
        });
		
		return rootView;
	}
	
	public void displayErrorMessage(String text) {
		//Fehlermeldung als Text anzeigen, wenn Text nicht leer
		if(text != null) {
			((TextView) this.rootView.findViewById(R.id.register_error_message)).setText(text);
		}
	}
}
