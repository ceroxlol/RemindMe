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
import java.text.SimpleDateFormat
import java.util.*

class ArrayAdapterAppointments(context: Context,
                               private var data: ArrayList<Appointment>?,
                               private val mLinearLayoutAppointments: LinearLayout) :
        ArrayAdapter<Appointment>(context, 0, data) {
    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val appointment = getItem(position)

        //TODO: ViewHolderPattern implementation, see recyclerview
        val rowView = inflater.inflate(R.layout.layout_appointments_list, parent, false)

        val textViewAppointmentName : TextView = rowView.findViewById(R.id.textViewSingleAppointmentName)
        val buttonDeleteAppointment : Button? = rowView.findViewById(R.id.buttonSingleAppointmentDelete)
        val linearLayoutAppointmentsFold : LinearLayout = rowView.findViewById(R.id.linearLayoutAppointmentsFold)

        val textViewAppointmentText : TextView = linearLayoutAppointmentsFold.findViewById(R.id.textViewUnfoldAppointmentText)
        val textViewAppointmentFavoriteLocation : TextView = linearLayoutAppointmentsFold.findViewById(R.id.textViewUnfoldAppointmentFavoriteLocation)
        val textViewAppointmentRemindTime : TextView = linearLayoutAppointmentsFold.findViewById(R.id.textViewUnfoldAppointmentTime)

        textViewAppointmentName.text = appointment!!.name
        textViewAppointmentText.text = appointment.appointmentText
        textViewAppointmentFavoriteLocation.text = MainActivity.mDatabaseHelper.favoriteLocationDaoRuntimeException.queryForId(appointment.favoriteLocation.id).name
        textViewAppointmentRemindTime.text = formateDate(appointment.appointmentRemindTime)

        textViewAppointmentName.setOnClickListener {
            if (linearLayoutAppointmentsFold.isShown)
                linearLayoutAppointmentsFold.visibility = View.GONE
            else
                linearLayoutAppointmentsFold.visibility = View.VISIBLE
        }

        linearLayoutAppointmentsFold.setOnClickListener{
            startEditSingleAppointment(appointment.id)
        }

        buttonDeleteAppointment?.setOnClickListener{
            data?.remove(appointment)
            this.notifyDataSetChanged()
            mLinearLayoutAppointments.removeView(rowView)
            MainActivity.mDatabaseHelper.appointmentDao.deleteById(appointment.id)
        }

        return rowView
    }

    private fun formateDate(remindTime: Date): String {
        val cal = Calendar.getInstance()
        cal.setTime(remindTime)
        return SimpleDateFormat("dd MM yyyy HH:mm").format(cal.time)
    }

    private fun startEditSingleAppointment(appointmentId: Int) {
        val i1 = Intent(getContext(), EditSingleAppointmentActivity::class.java)
        i1.putExtra("AppointmentID", appointmentId)
        context.startActivity(i1)
    }
}