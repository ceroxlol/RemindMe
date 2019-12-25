package com.example.ceroxlol.remindme

import Data.Appointment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*

class EditAppointmentActivity(
        private var mAppointments: ArrayList<Appointment?>? = null) : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)
    }

    init {
        mAppointments = MainActivity.mDatabaseHelper.daoAppointment.queryForAll() as ArrayList<Appointment?>
    }

}
