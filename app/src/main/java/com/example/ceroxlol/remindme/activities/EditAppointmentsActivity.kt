package com.example.ceroxlol.remindme.activities

import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.adapters.ArrayAdapterAppointments
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.activities.MainActivity.databaseHelper
import java.util.*

class EditAppointmentsActivity(
    private var appointmentArrayList: ArrayList<Appointment>? = ArrayList()
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)

        Log.i("EditAppointmentActivity", "Initializing edit appointments activity.")
        linearLayoutAppointments = findViewById(R.id.linearLayoutEditAppointmentsAppointments)
        appointmentArrayList = databaseHelper.appointmentDao.queryForAll() as ArrayList<Appointment>
        val mAppointmentsAdapter = ArrayAdapterAppointments(this, appointmentArrayList, linearLayoutAppointments)

        Log.i("EditAppointmentActivity", "Found " + mAppointmentsAdapter.count + " appointments.")
        for (i in 0 until mAppointmentsAdapter.count) {
            val view: View = mAppointmentsAdapter.getView(i, null, linearLayoutAppointments)
            linearLayoutAppointments.addView(view)
        }
    }

    companion object {
        //TODO: Fix this!
        private lateinit var linearLayoutAppointments: LinearLayout
    }
}
