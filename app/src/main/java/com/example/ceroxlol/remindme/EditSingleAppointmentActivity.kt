package com.example.ceroxlol.remindme

import Data.Appointment
import Data.FavoriteLocation
import Fragments.DatePickerFragment
import adapter.ArrayAdapterLocationsListSpinner
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditSingleAppointmentActivity : AppCompatActivity() {

    private val TAG = "EditSA"

    private lateinit var appointment: Appointment

    private lateinit var mButtonSingleAppointmentDate: Button
    private lateinit var mEditTextSingleAppointmentAppointmentName: EditText
    private lateinit var mEditTextSingleAppointmentAppointmentText: EditText
    private lateinit var mSpinnerSingleAppointmentLocations: Spinner
    private lateinit var mButtonSingleAppointmentSave : Button


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_single_appointment)

        this.mButtonSingleAppointmentDate = findViewById(R.id.button_single_appointment_date)
        this.mEditTextSingleAppointmentAppointmentName = findViewById(R.id.edit_text_single_appointment_appointment_name)
        this.mEditTextSingleAppointmentAppointmentText = findViewById(R.id.edit_text_single_appointment_appointment_text)
        this.mSpinnerSingleAppointmentLocations = findViewById(R.id.spinner_single_appointment_locations)
        this.mButtonSingleAppointmentSave = findViewById(R.id.button_single_appointment_save)

        this.mButtonSingleAppointmentDate.setOnClickListener {
            val datePickerDialog = DatePickerFragment()
            datePickerDialog.show(fragmentManager, "Date Picker")
        }

        this.mButtonSingleAppointmentSave.setOnClickListener{
            saveAppointment()
            finish()
        }

        val i = intent
        val id = i.getIntExtra("AppointmentID", -1)

        if(id == -1)
            Log.e(TAG, "AppointmentID couldn't be found. Something went wrong passing over the appointment.")

        loadAppointment(id)

        loadLocations()
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveAppointment() {
        appointment.name = this.mEditTextSingleAppointmentAppointmentName.text.toString()
        appointment.appointmentText = this.mEditTextSingleAppointmentAppointmentText.text.toString()
        val date_String = this.mButtonSingleAppointmentDate.text.toString()
        val date = SimpleDateFormat("dd MM yyyy HH:mm").parse(date_String)
        appointment.appointmentRemindTime = date
        MainActivity.mDatabaseHelper.appointmentDao.update(appointment)
        Log.i(TAG, "Saved appointment with the parameters \n${appointment.name} ${appointment.appointmentText} ${appointment.appointmentRemindTime}")
    }

    private fun loadAppointment(id: Int) {
        appointment = MainActivity.mDatabaseHelper.appointmentDao.queryForId(id)
        this.mEditTextSingleAppointmentAppointmentName.setText(appointment.name)
        this.mEditTextSingleAppointmentAppointmentText.setText(appointment.appointmentText)
        if (appointment.appointmentRemindTime != null) {
            val cal = Calendar.getInstance()
            cal.setTime(appointment.appointmentRemindTime)
            val dateFormat = SimpleDateFormat("dd MM yyyy HH:mm")
            Log.i(TAG, "${cal.time
            }")
            this.mButtonSingleAppointmentDate.text = dateFormat.format(cal.time)
        }
        Log.i(TAG, "Loaded appointment with parameters:\n" +
                "${appointment.name} \n" +
                "${appointment.appointmentText} \n" +
                "${appointment.appointmentRemindTime}")
    }

    private fun loadLocations() {
        val locations = MainActivity.mDatabaseHelper.favoriteLocationDao.queryForAll() as ArrayList<FavoriteLocation>
        val adapter = ArrayAdapterLocationsListSpinner(this, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        this.mSpinnerSingleAppointmentLocations.adapter = adapter
    }
}
