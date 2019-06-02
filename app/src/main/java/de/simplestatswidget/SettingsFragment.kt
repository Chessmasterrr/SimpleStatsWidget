package de.simplestatswidget

import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference

class SettingsFragment : PreferenceFragment() {

    // listener to set the edit preferences
    private val prefClick = Preference.OnPreferenceClickListener {
        val switchPref = findPreference("countReverse") as SwitchPreference
        val callMax = findPreference("callMax") as EditTextPreference
        val smsMax = findPreference("smsMax") as EditTextPreference

        val checked = switchPref.isChecked
        callMax.isEnabled = checked
        smsMax.isEnabled = checked
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set preference file
        val widgetId = (activity as SettingsActivity).mAppWidgetId
        preferenceManager.sharedPreferencesName = "prefs_$widgetId"

        addPreferencesFromResource(R.xml.settings)

        // add listener to switch
        val switchPref = findPreference("countReverse") as SwitchPreference
        switchPref.onPreferenceClickListener = prefClick

        // set state of the edit preferences
        val callMax = findPreference("callMax") as EditTextPreference
        val smsMax = findPreference("smsMax") as EditTextPreference
        val checked = switchPref.isChecked
        callMax.isEnabled = checked
        smsMax.isEnabled = checked
    }
}
