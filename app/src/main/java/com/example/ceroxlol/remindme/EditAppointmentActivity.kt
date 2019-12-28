package com.example.ceroxlol.remindme

import Adapter.ArrayAdapterAppointments
import Data.Appointment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import java.util.*

class EditAppointmentActivity(
        private var mAppointments: ArrayList<Appointment>? = null) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)

        Log.i("EditAppointmentActivity", "Setting up edit appointments activity.")
        mAppointments = MainActivity.mDatabaseHelper.appointmentDao.queryForAll() as ArrayList<Appointment>
        val mAppointmentsAdapter = ArrayAdapterAppointments(this, mAppointments)
        val mLinearLayoutAppointments = findViewById<LinearLayout>(R.id.linearLayoutEditAppointmentsAppointments)

        Log.i("EditAppointmentActivity", "Found " + mAppointmentsAdapter.count + "appointments.")
        for (i in 0 until mAppointmentsAdapter.count){
            val view : View = mAppointmentsAdapter.getView(i, null, null)
            mLinearLayoutAppointments.addView(view)
        }
    }
}
