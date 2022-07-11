package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.ceroxlol.remindme.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}