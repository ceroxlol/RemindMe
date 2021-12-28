package com.example.ceroxlol.remindme.adapters

import com.example.ceroxlol.remindme.models.Appointment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.R


class RecyclerViewListAdapterAppointments(private var list: List<Appointment>) :
    RecyclerView.Adapter<AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppointmentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment: Appointment = list[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = list.size

    fun setAppointmentList(list: List<Appointment>) {
        this.list = list
    }
}

class AppointmentViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.layout_appointments_main_activity,
            parent,
            false
        )
    ) {
    private var textViewAppointmentName: TextView? = null
    private var textViewAppointmentText: TextView? = null
    private var textViewAppointmentType: TextView? = null
    private var textViewAppointmentPrio: TextView? = null

    init {
        textViewAppointmentName = itemView.findViewById(R.id.text_view_appointment_name)
        textViewAppointmentText = itemView.findViewById(R.id.text_view_appointment_text)
        textViewAppointmentType = itemView.findViewById(R.id.text_view_appointment_type)
        textViewAppointmentPrio = itemView.findViewById(R.id.text_view_appointment_prio)
    }

    fun bind(appointment: Appointment) {
        textViewAppointmentName?.text = appointment.name
        textViewAppointmentText?.text = appointment.appointmentText
        textViewAppointmentType?.text = appointment.type.toString()
        textViewAppointmentPrio?.text = appointment.priority.toString()
    }
}