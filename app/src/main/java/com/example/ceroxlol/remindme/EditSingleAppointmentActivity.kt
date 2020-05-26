package com.example.ceroxlol.remindme

import Fragments.DatePickerFragment
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText

class EditSingleAppointmentActivity : AppCompatActivity() {

    private val TAG = "EditSA"

    private lateinit var mButtonSingleAppointmentDate: Button
    private lateinit var mEditTextSingleAppointmentAppointmentName: EditText
    private lateinit var mEditTextSingleAppointmentAppointmentText: EditText


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_single_appointment)

        this.mButtonSingleAppointmentDate = findViewById(R.id.button_single_appointment_date)
        this.mEditTextSingleAppointmentAppointmentName = findViewById(R.id.edit_text_single_appointment_appointment_name)
        this.mEditTextSingleAppointmentAppointmentText = findViewById(R.id.edit_text_single_appointment_appointment_text)

        this.mButtonSingleAppointmentDate.setOnClickListener {
            val datePickerDialog = DatePickerFragment()
            datePickerDialog.show(fragmentManager, "Date Picker")
        }

        val i = intent
        val id = i.getIntExtra("AppointmentID", -1)

        if(id == -1)
            Log.e(TAG, "AppointmentID couldn't be found. Something went wrong passing over the appointment.")

        loadAppointment(id)

        loadLocations()
    }

    private fun loadAppointment(id: Int) {
        val appointment = MainActivity.mDatabaseHelper.appointmentDao.queryForId(id)
        this.mEditTextSingleAppointmentAppointmentName.setText(appointment.name)
        this.mEditTextSingleAppointmentAppointmentText.setText(appointment.appointmentText)
    }

    private fun loadLocations() {

    }
}
