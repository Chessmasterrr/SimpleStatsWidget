package de.simplestatswidget;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.telephony.TelephonyManager;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // get current phonenumber
        String number = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelephonyManager tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            try {
                number = tMgr.getLine1Number();
            } catch (SecurityException e) {
                // missing permission to get the number
                number = "";
            }
        }

        // set phonenumber in settings
        EditTextPreference phonennumberPreference = (EditTextPreference) findPreference("phonenumber");
        phonennumberPreference.setText(number);
    }
}
