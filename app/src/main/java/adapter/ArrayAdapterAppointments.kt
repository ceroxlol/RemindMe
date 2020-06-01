package adapter

import Data.Appointment
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.ceroxlol.remindme.EditSingleAppointmentActivity
import com.example.ceroxlol.remindme.MainActivity
import com.example.ceroxlol.remindme.R
import java.util.ArrayList

class ArrayAdapterAppointments(context: Context,
                               private var data: ArrayList<Appointment>?,
                               val mLinearLayoutAppointments: LinearLayout) :
        ArrayAdapter<Appointment>(context, 0, data) {
    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val appointment = getItem(position)

        //TODO: ViewHolderPattern implementation, see recyclerview
        val rowView = inflater.inflate(R.layout.layout_appointments_list, parent, false)

        val textViewAppointmentName : TextView = rowView.findViewById(R.id.textViewSingleAppointmentName)
        //TODO: Delete this button. Make the textView clickable (onClick)
        val buttonEditAppointment: Button? = rowView.findViewById(R.id.buttonEditSingleAppointment)
        val buttonDeleteAppointment : Button? = rowView.findViewById(R.id.buttonDeleteSingleAppointment)

        textViewAppointmentName.text = appointment!!.name
        buttonEditAppointment?.setOnClickListener {
            val i1 = Intent(getContext(), EditSingleAppointmentActivity::class.java)
            i1.putExtra("AppointmentID", appointment.id)
            context.startActivity(i1)
        }
        buttonDeleteAppointment?.setOnClickListener{
            data?.remove(appointment)
            this.notifyDataSetChanged()
            mLinearLayoutAppointments.removeView(rowView)
            MainActivity.mDatabaseHelper.appointmentDao.deleteById(appointment.id)
        }

        return rowView
    }
}