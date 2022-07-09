package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.AppointmentListAdapter
import com.example.ceroxlol.remindme.databinding.AppointmentListFragmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory

class AppointmentsListFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by activityViewModels {
        AppointmentViewModelFactory(
            (activity?.application as RemindMeApplication).database.appointmentDao()
        )
    }

    //TODO: Make elements deletable by clicking long or moving them to the side
    private var _binding: AppointmentListFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var appointmentList : List<Appointment>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AppointmentListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppointmentListAdapter (
            {
                val action =
                    MainFragmentDirections.actionMainFragmentToEditAppointmentFragment(it.id)
                this.findNavController().navigate(action)
            },
            {
                //TODO: Show snackbar for undo
                appointmentViewModel.deleteAppointment(it)
                true
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        appointmentViewModel.allAppointments.observe(this.viewLifecycleOwner) { appointments ->
            appointments.let {
                appointmentList = it
                adapter.submitList(filterAppointments())
            }
        }

        binding.addNewLocationButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddAppointmentFragment()
            this.findNavController().navigate(action)
        }

        binding.checkBoxDone.setOnCheckedChangeListener { _, _ ->
            adapter.submitList(filterAppointments())
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterAppointments(): List<Appointment> {
        if(!binding.checkBoxDone.isChecked){
            return appointmentList.filter {
                !it.done
            }
        }
        //TODO: move this to database?
        return appointmentList.sortedBy { it.done }
    }

}
