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
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModelFactory

class AppointmentsListFragment : Fragment() {

    private val appointmentKTViewModel: AppointmentKTViewModel by activityViewModels {
        AppointmentKTViewModelFactory(
            (activity?.application as RemindMeApplication).database.appointmentDao()
        )
    }

    private var _binding: AppointmentListFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AppointmentListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppointmentListAdapter {
            val action =
                MainFragmentDirections.actionMainFragmentToEditAppointmentFragment()
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        appointmentKTViewModel.allAppointments.observe(this.viewLifecycleOwner) { appointments ->
            appointments.let {
                adapter.submitList(it)
            }
        }

        binding.saveButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddAppointmentFragment()
            this.findNavController().navigate(action)
        }
    }
}