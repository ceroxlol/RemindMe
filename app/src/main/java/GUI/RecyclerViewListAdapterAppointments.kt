package GUI

import Data.Appointment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.example.ceroxlol.remindme.R


class RecyclerViewListAdapterAppointments(private val list: List<Appointment>)
    : RecyclerView.Adapter<AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppointmentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment: Appointment = list[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = list.size

}

class AppointmentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.layout_appointments_main_activity, parent, false)) {
    private var mTextViewAppointmentName: TextView? = null
    private var mTextViewAppointmentText: TextView? = null
    private var mTextViewAppointmentType: TextView? = null
    private var mTextViewAppointmentPrio: TextView? = null

    init {
        mTextViewAppointmentName = itemView.findViewById(R.id.text_view_appointment_name)
        mTextViewAppointmentText = itemView.findViewById(R.id.text_view_appointment_text)
        mTextViewAppointmentType = itemView.findViewById(R.id.text_view_appointment_type)
        mTextViewAppointmentPrio = itemView.findViewById(R.id.text_view_appointment_prio)
    }

    fun bind(appointment: Appointment) {
        mTextViewAppointmentName?.text = appointment.name
        mTextViewAppointmentText?.text = appointment.appointmentText
        mTextViewAppointmentType?.text = appointment.type.toString()
        mTextViewAppointmentPrio?.text = appointment.priority.toString()
    }
}