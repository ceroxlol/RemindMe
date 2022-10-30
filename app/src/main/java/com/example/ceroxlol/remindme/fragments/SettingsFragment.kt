package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import androidx.preference.*
import com.example.ceroxlol.remindme.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val locationUpdatePreference = SeekBarPreference(context)
        locationUpdatePreference.key = "location_update_distance"
        locationUpdatePreference.setDefaultValue(0)
        locationUpdatePreference.min = 0
        locationUpdatePreference.showSeekBarValue = true
        locationUpdatePreference.max = 10
        locationUpdatePreference.seekBarIncrement = 2
        locationUpdatePreference.title = "Movement trigger"
        locationUpdatePreference.summary = "Minimum movement in meters to trigger updates"
        locationUpdatePreference.layoutResource = R.layout.seekbarpreference
        /*val locationUpdatePreference: SeekBarPreference = findPreference("location_update_distance")!!
        locationUpdatePreference.max = 1000
        locationUpdatePreference.seekBarIncrement = 200*/

        val locationCategory = PreferenceCategory(context)
        locationCategory.title = resources.getString(R.string.locations)

        screen.addPreference(locationCategory)
        locationCategory.addPreference(locationUpdatePreference)


        val snoozeTimerPreference = EditTextPreference(context)
        snoozeTimerPreference.key = "snooze"
        snoozeTimerPreference.setDefaultValue("10")
        snoozeTimerPreference.title = resources.getString(R.string.snooze_timer)
        snoozeTimerPreference.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()

        val appointmentUpdatePreference = SeekBarPreference(context)
        appointmentUpdatePreference.key = "appointment_update_distance"
        appointmentUpdatePreference.setDefaultValue(500)
        appointmentUpdatePreference.min = 0
        appointmentUpdatePreference.showSeekBarValue = true
        appointmentUpdatePreference.max = 20000
        appointmentUpdatePreference.seekBarIncrement = 500
        appointmentUpdatePreference.title = ""
        appointmentUpdatePreference.summary = "Trigger distance to an appointment in meters"

        /*val appointmentUpdatePreference: SeekBarPreference = findPreference("appointment_update_distance")!!
        appointmentUpdatePreference.max = 20000
        appointmentUpdatePreference.seekBarIncrement = 500*/

        val appointmentCategory = PreferenceCategory(context)
        appointmentCategory.title = resources.getString(R.string.appointments)

        screen.addPreference(appointmentCategory)
        appointmentCategory.addPreference(snoozeTimerPreference)
        appointmentCategory.addPreference(appointmentUpdatePreference)

        val deleteAppointmentsAfterPreference = SeekBarPreference(context)
        deleteAppointmentsAfterPreference.setDefaultValue(30)
        deleteAppointmentsAfterPreference.key = "delete_appointments_after"
        deleteAppointmentsAfterPreference.min = 30
        deleteAppointmentsAfterPreference.showSeekBarValue = true
        deleteAppointmentsAfterPreference.title = "Delete appointments after ${deleteAppointmentsAfterPreference.value} days"

        val systemOfUnitsPreference = SwitchPreference(context)
        systemOfUnitsPreference.key = "system_of_units"
        systemOfUnitsPreference.setDefaultValue(false)
        systemOfUnitsPreference.switchTextOn = "MI"
        systemOfUnitsPreference.switchTextOff = "KM"
        systemOfUnitsPreference.summaryOn = "MI"
        systemOfUnitsPreference.summaryOff = "KM"
        systemOfUnitsPreference.title = "System of units"
        /*systemOfUnits.summaryProvider =
            Preference.SummaryProvider<SwitchPreference> { preference ->
                if(preference.isChecked) {
                    "MI"
                }
                else{
                    "KM"
                }
            }*/
        val comingSoonCategory = PreferenceCategory(context)
        comingSoonCategory.title = "Coming Soon!"
        screen.addPreference(comingSoonCategory)
        comingSoonCategory.addPreference(deleteAppointmentsAfterPreference)
        comingSoonCategory.addPreference(systemOfUnitsPreference)

        preferenceScreen = screen
    }
}