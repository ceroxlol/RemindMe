package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.activities.MainActivity.databaseHelper
import com.example.ceroxlol.remindme.adapters.AppointmentViewHolder
import com.example.ceroxlol.remindme.adapters.RecyclerViewListAdapterAppointments
import com.example.ceroxlol.remindme.models.Appointment

class AppointmentsFragment : Fragment() {

    private var data : ArrayList<Appointment> = ArrayList()
    private val adapter : RecyclerView.Adapter<AppointmentViewHolder> = initAdapter()

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
        recyclerView.adapter = adapter

        return view
    }

    private fun initAdapter(): RecyclerView.Adapter<AppointmentViewHolder> {
        data = databaseHelper.appointmentDaoRuntimeException.queryForAll() as ArrayList<Appointment>
        return RecyclerViewListAdapterAppointments(data)
    }

    fun insertData(appointment: Appointment){
        data.add(appointment)
        adapter.notifyDataSetChanged()
    }

}