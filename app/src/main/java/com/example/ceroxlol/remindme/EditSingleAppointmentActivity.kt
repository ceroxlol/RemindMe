package com.example.ceroxlol.remindme

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.widget.CheckBox
import android.widget.TextView
import java.util.*

class EditSingleAppointmentActivity() : AppCompatActivity() {

    lateinit var mCheckBoxHasTime: CheckBox
    lateinit var mTextViewDateTime: TextView

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_single_appointment)

        mCheckBoxHasTime = findViewById(R.id.check_box_single_appointment_has_time)
        mTextViewDateTime = findViewById(R.id.text_view_date_time)

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            mTextViewDateTime.text = "$dayOfMonth $monthOfYear, $year"
        }, year, month, day)

        mCheckBoxHasTime.setOnClickListener {
            datePickerDialog.show()
        }
    }
}
