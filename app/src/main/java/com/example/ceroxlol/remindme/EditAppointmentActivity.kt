package com.example.ceroxlol.remindme

import Data.Appointment
import adapter.ArrayAdapterAppointments
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import java.util.*

class EditAppointmentActivity(
        private var mAppointmentArrayList: ArrayList<Appointment>? = null) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)

        Log.i("EditAppointmentActivity", "Initializing edit appointments activity.")
        mLinearLayoutAppointments = findViewById(R.id.linearLayoutEditAppointmentsAppointments)
        mAppointmentArrayList = MainActivity.mDatabaseHelper.appointmentDao.queryForAll() as ArrayList<Appointment>
        val mAppointmentsAdapter = ArrayAdapterAppointments(this, mAppointmentArrayList, mLinearLayoutAppointments)

        Log.i("EditAppointmentActivity", "Found " + mAppointmentsAdapter.count + " appointments.")
        for (i in 0 until mAppointmentsAdapter.count) {
            val view: View = mAppointmentsAdapter.getView(i, null, mLinearLayoutAppointments)
            mLinearLayoutAppointments.addView(view)
        }
    }

    companion object {
        private lateinit var mLinearLayoutAppointments: LinearLayout
    }
}
