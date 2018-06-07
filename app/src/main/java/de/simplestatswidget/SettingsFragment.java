package de.simplestatswidget;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragment {

    // listener to set the edit preferences
    private final Preference.OnPreferenceClickListener pref_click = new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            SwitchPreference switchPref = (SwitchPreference) findPreference("countReverse");
            EditTextPreference callMax = (EditTextPreference) findPreference("callMax");
            EditTextPreference smsMax = (EditTextPreference) findPreference("smsMax");

            Boolean checked = switchPref.isChecked();
            callMax.setEnabled(checked);
            smsMax.setEnabled(checked);
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        // add listener to switch
        SwitchPreference switchPref = (SwitchPreference) findPreference("countReverse");
        switchPref.setOnPreferenceClickListener(pref_click);

        // set state of the edit preferences
        EditTextPreference callMax = (EditTextPreference) findPreference("callMax");
        EditTextPreference smsMax = (EditTextPreference) findPreference("smsMax");
        Boolean checked = switchPref.isChecked();
        callMax.setEnabled(checked);
        smsMax.setEnabled(checked);
    }
}
