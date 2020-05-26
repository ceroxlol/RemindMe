package com.example.ceroxlol.remindme

import Fragments.DatePickerFragment
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class EditSingleAppointmentActivity : AppCompatActivity() {

    private lateinit var mButtonSingleAppointmentDate: Button

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_single_appointment)

        this.mButtonSingleAppointmentDate = findViewById(R.id.button_single_appointment_date)

        this.mButtonSingleAppointmentDate.setOnClickListener {
            val datePickerDialog = DatePickerFragment()
            datePickerDialog.show(fragmentManager, "Date Picker")
        }
    }
}
