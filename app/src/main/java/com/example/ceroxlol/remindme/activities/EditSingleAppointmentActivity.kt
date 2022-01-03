package com.example.ceroxlol.remindme.activities

import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.FavoriteLocation
import com.example.ceroxlol.remindme.fragments.DatePickerFragment
import com.example.ceroxlol.remindme.adapters.ArrayAdapterLocationsListSpinner
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.activities.MainActivity.databaseHelper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class EditSingleAppointmentActivity : AppCompatActivity() {

    private val TAG = "EditSA"

    private lateinit var appointment: Appointment

    private lateinit var buttonSingleAppointmentDate: Button
    private lateinit var editTextSingleAppointmentAppointmentName: EditText
    private lateinit var editTextSingleAppointmentAppointmentText: EditText
    private lateinit var spinnerSingleAppointmentLocations: Spinner
    private lateinit var buttonSingleAppointmentSave: Button
    private lateinit var buttonSingleAppointmentCleareDate: Button


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_single_appointment)

        this.buttonSingleAppointmentCleareDate = findViewById(R.id.buttonSingleAppointmentClearDate)
        this.buttonSingleAppointmentDate = findViewById(R.id.buttonSingleAppointmentDate)
        this.editTextSingleAppointmentAppointmentName =
            findViewById(R.id.editTextSingleAppointmentAppointmentName)
        this.editTextSingleAppointmentAppointmentText =
            findViewById(R.id.editTextSingleAppointmentAppointmentText)
        this.spinnerSingleAppointmentLocations =
            findViewById(R.id.spinnerSingleAppointmentLocations)
        this.buttonSingleAppointmentSave = findViewById(R.id.buttonSingleAppointmentSave)

        this.buttonSingleAppointmentDate.setOnClickListener {
            val datePickerDialog = DatePickerFragment(R.id.buttonSingleAppointmentDate)
            //TODO: Remove FragmentManager
            datePickerDialog.show(fragmentManager, "Date Picker")
        }

        this.buttonSingleAppointmentCleareDate.setOnClickListener {
            this.buttonSingleAppointmentDate.text = "No Date"
        }

        this.buttonSingleAppointmentSave.setOnClickListener {
            saveAppointment()
            finish()
        }

        val i = intent
        val id = i.getIntExtra("AppointmentID", -1)

        if (id == -1)
            Log.e(
                TAG,
                "AppointmentID couldn't be found. Something went wrong passing over the appointment."
            )

        loadAppointment(id)

        loadLocations()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_single_appointment_is_active, menu)
        val item = menu!!.findItem(R.id.switchForActionBar)
        item.setActionView(R.layout.switch_layout)

        val mySwitch = item.actionView.findViewById<SwitchCompat>(R.id.switchForActionBar)
        mySwitch.isChecked = appointment.isActive
        mySwitch.setOnCheckedChangeListener { p0, isChecked ->
            appointment.isActive = isChecked
            databaseHelper.appointmentDao.update(appointment)
            Log.d(TAG, "Set appointment " + appointment.id + " isActive to " + appointment.isActive)
        }

        return true
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveAppointment() {
        appointment.name = this.editTextSingleAppointmentAppointmentName.text.toString()
        appointment.appointmentText = this.editTextSingleAppointmentAppointmentText.text.toString()
        val date_String = this.buttonSingleAppointmentDate.text.toString()
        try {
            val date = SimpleDateFormat("dd MM yyyy HH:mm").parse(date_String)
            appointment.appointmentTime = date
        } catch (exception: ParseException) {
            appointment.appointmentTime = null
            Log.e(TAG, "Couldn't parse the date. Set it to 'null'")
            Log.e(TAG, exception.toString())
        }
        appointment.favoriteLocation =
            this.spinnerSingleAppointmentLocations.selectedItem as FavoriteLocation
        databaseHelper.appointmentDao.update(appointment)

        Log.i(
            TAG,
            "Saved appointment with the parameters \n${appointment.name} ${appointment.appointmentText} ${appointment.appointmentTime}"
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadAppointment(id: Int) {
        appointment = MainActivity.databaseHelper.appointmentDaoRuntimeException.queryForId(id)

        this.editTextSingleAppointmentAppointmentName.setText(appointment.name)
        this.editTextSingleAppointmentAppointmentText.setText(appointment.appointmentText)
        if (appointment.appointmentTime != null) {
            val cal = Calendar.getInstance()
            cal.time = appointment.appointmentTime
            val dateFormat = SimpleDateFormat("dd MM yyyy HH:mm")
            Log.i(TAG, "${cal.time}")
            this.buttonSingleAppointmentDate.text = dateFormat.format(cal.time)
        }
        Log.i(
            TAG, "Loaded appointment:\n" +
                    "${appointment.name} \n" +
                    "${appointment.appointmentText} \n" +
                    "${appointment.appointmentTime}"
        )
    }

    private fun loadLocations() {
        val locations = databaseHelper.favoriteLocationDao.queryForAll() as ArrayList<FavoriteLocation>
        val adapter = ArrayAdapterLocationsListSpinner(this, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        this.spinnerSingleAppointmentLocations.adapter = adapter
        val position = getFavoriteLocationPositionWithID(locations)
        this.spinnerSingleAppointmentLocations.setSelection(position)
    }

    private fun getFavoriteLocationPositionWithID(locations: ArrayList<FavoriteLocation>): Int {
        locations.forEach { location ->
            if (location.id == appointment.favoriteLocation.id)
                return locations.indexOf(location)
        }
        return 0
    }
}
