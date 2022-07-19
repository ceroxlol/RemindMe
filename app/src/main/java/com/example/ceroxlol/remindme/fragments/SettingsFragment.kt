package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.example.ceroxlol.remindme.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val locationUpdatePreference: SeekBarPreference? = findPreference("location_update_distance")
        locationUpdatePreference?.max = 5000
        locationUpdatePreference?.seekBarIncrement = 200

        val appointmentUpdatePreference: SeekBarPreference? = findPreference("appointment_update_distance")
        appointmentUpdatePreference?.max = 5000
        appointmentUpdatePreference?.seekBarIncrement = 200

        val deleteAppointmentsAfter: SeekBarPreference? = findPreference("delete_appointments_after")
        deleteAppointmentsAfter?.title = "Delete appointments after ${deleteAppointmentsAfter?.value} days"

        val systemOfUnits: SwitchPreference? = findPreference("system_of_units")
        systemOfUnits?.summaryProvider =
            Preference.SummaryProvider<SwitchPreference> { preference ->
                if(preference.isChecked) {
                    "MI"
                }
                else{
                    "KM"
                }
            }
    }
}