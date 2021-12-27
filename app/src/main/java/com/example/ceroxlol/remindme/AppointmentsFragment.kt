package com.example.ceroxlol.remindme

import Data.Appointment
import adapter.AppointmentViewHolder
import adapter.RecyclerViewListAdapterAppointments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.MainActivity.mDatabaseHelper

class AppointmentsFragment : Fragment() {

    companion object {
        fun newInstance() = AppointmentsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_appointments, container, false)

        val recyclerView : RecyclerView = view.findViewById(R.id.RecyclerViewAppointmentList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = getAdapter()

        return view
    }

    private fun getAdapter(): RecyclerView.Adapter<AppointmentViewHolder> {
        val appointmentArrayList : ArrayList<Appointment> =
            mDatabaseHelper.appointmentDaoRuntimeException.queryForAll() as ArrayList<Appointment>
        return RecyclerViewListAdapterAppointments(appointmentArrayList)
    }
}