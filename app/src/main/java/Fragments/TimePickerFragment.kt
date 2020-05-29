package Fragments

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.app.DialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.widget.Button
import java.util.Calendar
import android.widget.TimePicker
import android.widget.Toast
import com.example.ceroxlol.remindme.R
import java.text.SimpleDateFormat


@SuppressLint("ValidFragment")
class TimePickerFragment(private val dateButtonId:Int, private val year: Int, private val month: Int, private val day: Int) : DialogFragment(), TimePickerDialog.OnTimeSetListener {
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
                true // Is 24 hour view
        )
    }


    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the returned time
        val button:Button = activity.findViewById(dateButtonId) as Button
        button.text = formatDate(year, month, day, hourOfDay, minute)
    }


    // When user cancel the time picker dialog
    override fun onCancel(dialog: DialogInterface?) {
        Toast.makeText(activity,"Canceled.",Toast.LENGTH_SHORT).show()
        val button:Button = activity.findViewById(dateButtonId) as Button
        button.text = "@string/no_date"
        super.onCancel(dialog)
    }


    // Custom method to format date
    private fun formatDate(year:Int, month:Int, day:Int, hourOfDay: Int, minute: Int):String{
        // Create a Date variable/object with user chosen date
        calendar.set(year, month, day, hourOfDay, minute, 0)
        val chosenDate = calendar.time

        return SimpleDateFormat("dd MM yyyy HH:mm").format(chosenDate)
    }
}