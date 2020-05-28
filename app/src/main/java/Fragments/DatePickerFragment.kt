package Fragments

import android.app.*
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import android.widget.DatePicker
import android.widget.Toast
import com.example.ceroxlol.remindme.R
import java.text.SimpleDateFormat
import java.util.Calendar


class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private lateinit var calendar:Calendar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize a calendar instance
        calendar = Calendar.getInstance()

        // Get the system current date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)


        // Initialize a new date picker dialog and return it
        return DatePickerDialog(
                activity, // Context
                // Put 0 to system default theme or remove this parameter
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth, // Theme
                this, // DatePickerDialog.OnDateSetListener
                year, // Year
                month, // Month of year
                day // Day of month
        )
    }


    // When date set and press ok button in date picker dialog
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        Toast.makeText(
                activity,
                "Date Set : ${formatDate(year,month,day)}"
                ,Toast.LENGTH_SHORT
        ).show()

        // Display the selected date in text view
        //activity.findViewById<TextView>(R.id.button_single_appointment_date).text = formatDate(year,month,day)

        val timePickerFragment = TimePickerFragment(year, month, day)
        timePickerFragment.show(fragmentManager, "Time Picker")
    }


    // Custom method to format date
    private fun formatDate(year:Int, month:Int, day:Int):String{
        // Create a Date variable/object with user chosen date
        calendar.set(year, month, day, 0, 0, 0)
        val chosenDate = calendar.time

        return SimpleDateFormat("dd MM yyyy HH:mm").format(chosenDate)
    }

    override fun onCancel(dialog: DialogInterface?) {
        activity.findViewById<TextView>(R.id.button_single_appointment_date).text = "@string/no_date"
        Toast.makeText(getActivity(),"Date Picker Canceled.", Toast.LENGTH_SHORT).show()
    }
}