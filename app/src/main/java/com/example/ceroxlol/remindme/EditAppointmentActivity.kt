package com.example.ceroxlol.remindme

import Data.Appointment
import adapter.ArrayAdapterAppointments
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class EditAppointmentActivity(
    private var appointmentArrayList: ArrayList<Appointment>? = null
) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)

        Log.i("EditAppointmentActivity", "Initializing edit appointments activity.")
        linearLayoutAppointments = findViewById(R.id.linearLayoutEditAppointmentsAppointments)
        appointmentArrayList =
            MainActivity.mDatabaseHelper.appointmentDao.queryForAll() as ArrayList<Appointment>
        val mAppointmentsAdapter =
            ArrayAdapterAppointments(this, appointmentArrayList, linearLayoutAppointments)

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
