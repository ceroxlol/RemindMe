package Fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.app.DialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.widget.Button
import java.util.Calendar
import android.widget.TimePicker
import android.widget.Toast
import com.example.ceroxlol.remindme.R


class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var calendar:Calendar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize a Calendar instance
        calendar = Calendar.getInstance()

        // Get the system current hour and minute
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)


        // Create a TimePickerDialog with system current time
        // Return the TimePickerDialog
        return TimePickerDialog(
                activity, // Context
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth, // Theme
                this, // TimePickerDialog.OnTimeSetListener
                hour, // Hour of day
                minute, // Minute
                false // Is 24 hour view
        )
    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the returned time
        val button:Button = activity.findViewById(R.id.button_single_appointment_date) as Button
        button.text = button.text.toString() + " ${getHourAMPM(hourOfDay)}:$minute ${getAMPM(hourOfDay)}"
    }


    // When user cancel the time picker dialog
    override fun onCancel(dialog: DialogInterface?) {
        Toast.makeText(activity,"Canceled.",Toast.LENGTH_SHORT).show()
        val button:Button = activity.findViewById(R.id.button_single_appointment_date) as Button
        button.text = "@string/no_date"
        super.onCancel(dialog)
    }


    // Custom method to get AM PM value from provided hour
    private fun getAMPM(hour:Int):String{
        return if(hour>11)"PM" else "AM"
    }


    // Custom method to get hour for AM PM time format
    private fun getHourAMPM(hour:Int):Int{
        // Return the hour value for AM PM time format
        var modifiedHour = if (hour>11)hour-12 else hour
        if (modifiedHour == 0){modifiedHour = 12}
        return modifiedHour
    }
}