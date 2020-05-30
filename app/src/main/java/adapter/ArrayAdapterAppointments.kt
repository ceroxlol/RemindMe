package adapter

import Data.Appointment
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.ceroxlol.remindme.EditSingleAppointmentActivity
import com.example.ceroxlol.remindme.MainActivity
import com.example.ceroxlol.remindme.R
import java.util.ArrayList

class ArrayAdapterAppointments(context: Context,
                               data: ArrayList<Appointment>?) :
        ArrayAdapter<Appointment>(context, 0, data) {
    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val appointment = getItem(position)

        //TODO: ViewHolderPattern implementation
        val rowView = inflater.inflate(R.layout.layout_appointments_list, parent, false)

        val textViewAppointmentName : TextView = rowView.findViewById(R.id.textViewSingleAppointmentName)
        val buttonEditAppointment: Button? = rowView.findViewById(R.id.buttonEditSingleAppointment)
        val buttonDeleteAppointment : Button? = rowView.findViewById(R.id.buttonDeleteSingleAppointment)

        textViewAppointmentName.text = appointment!!.name
        buttonEditAppointment?.setOnClickListener {
            val i1 = Intent(getContext(), EditSingleAppointmentActivity::class.java)
            i1.putExtra("AppointmentID", appointment.id)
            context.startActivity(i1)
        }
        buttonDeleteAppointment?.setOnClickListener{
            MainActivity.mDatabaseHelper.appointmentDao.deleteById(appointment.id)
            //TODO: Refresh the list
        }

        return rowView
    }
}