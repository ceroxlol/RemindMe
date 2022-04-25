package com.example.ceroxlol.remindme.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.models.AppointmentKT


class RecyclerViewListAdapterAppointments(private val list: List<AppointmentKT>) :
    RecyclerView.Adapter<AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AppointmentViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment: AppointmentKT = list[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = list.size
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

    init {
        textViewAppointmentName = itemView.findViewById(R.id.text_view_appointment_name)
        textViewAppointmentText = itemView.findViewById(R.id.text_view_appointment_text)
    }

    fun bind(appointment: AppointmentKT) {
        textViewAppointmentName?.text = appointment.name
        textViewAppointmentText?.text = appointment.text
    }
}