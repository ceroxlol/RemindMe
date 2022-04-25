package com.example.ceroxlol.remindme.activities

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
import com.example.ceroxlol.remindme.activities.MainActivity.getDb
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.LocationMarker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class EditSingleAppointmentActivity : AppCompatActivity() {

    private val TAG = "EditSA"

    private lateinit var appointmentKT: AppointmentKT

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
        mySwitch.isChecked = !appointmentKT.done
        mySwitch.setOnCheckedChangeListener { p0, isChecked ->
            appointmentKT.done = !isChecked
            getDb().appointmentDao().update(appointmentKT)
            Log.d(TAG, "Set appointment ${appointmentKT.id} to done ${appointmentKT.done}")
        }

        return true
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveAppointment() {
        appointmentKT.name = this.editTextSingleAppointmentAppointmentName.text.toString()
        appointmentKT.text = this.editTextSingleAppointmentAppointmentText.text.toString()
        val dateString = this.buttonSingleAppointmentDate.text.toString()
        try {
            val date = SimpleDateFormat("dd MM yyyy HH:mm").parse(dateString)
            appointmentKT.time = date
        } catch (exception: ParseException) {
            appointmentKT.time = null
            Log.e(TAG, "Couldn't parse the date. Set it to 'null'")
            Log.e(TAG, exception.toString())
        }
        appointmentKT.location = this.spinnerSingleAppointmentLocations.selectedItem as LocationMarker
        getDb().appointmentDao().update(appointmentKT)

        Log.i(
            TAG,
            "Saved appointment with the parameters \n${appointmentKT.name} ${appointmentKT.text} ${appointmentKT.time}"
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadAppointment(id: Int) {
        appointmentKT = getDb().appointmentDao().getById(id)

        this.editTextSingleAppointmentAppointmentName.setText(appointmentKT.name)
        this.editTextSingleAppointmentAppointmentText.setText(appointmentKT.text)
        if (appointmentKT.time != null) {
            val cal = Calendar.getInstance()
            cal.time = appointmentKT.time
            val dateFormat = SimpleDateFormat("dd MM yyyy HH:mm")
            Log.i(TAG, "${cal.time}")
            this.buttonSingleAppointmentDate.text = dateFormat.format(cal.time)
        }
        Log.i(
            TAG, "Loaded appointment:\n" +
                    "${appointmentKT.name} \n" +
                    "${appointmentKT.text} \n" +
                    "${appointmentKT.time}"
        )
    }

    private fun loadLocations() {
        val locations = getDb().locationMarkerDao().getAll() as ArrayList<LocationMarker>
        val adapter = ArrayAdapterLocationsListSpinner(this, locations)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        this.spinnerSingleAppointmentLocations.adapter = adapter
        val position = getFavoriteLocationPositionWithID(locations)
        this.spinnerSingleAppointmentLocations.setSelection(position)
    }

    private fun getFavoriteLocationPositionWithID(locations: ArrayList<LocationMarker>): Int {
        locations.forEach { location ->
            if (location.id == appointmentKT.location.id)
                return locations.indexOf(location)
        }
        return 0
    }
}
