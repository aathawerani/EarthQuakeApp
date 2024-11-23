package com.example.earthquake

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat


class PreferencesActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preferences)
    }

    companion object {
        val PREF_MIN_MAG = "PREF_MIN_MAG"
        val PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE"
        val USER_PREFERENCE = "USER_PREFERENCE"
        val PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ"
    }
}

class PrefFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.userpreferences, null)
    }
}
