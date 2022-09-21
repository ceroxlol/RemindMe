package com.example.ceroxlol.remindme.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerAndAppointmentListAdapter
import com.example.ceroxlol.remindme.databinding.FragmentListAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.*
import com.google.android.material.snackbar.Snackbar


class AppointmentsListFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by activityViewModels {
        AppointmentViewModelFactory(
            (activity?.application as RemindMeApplication).database.appointmentDao()
        )
    }

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private var _binding: FragmentListAppointmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LocationMarkerAndAppointmentListAdapter(
            {
                val action =
                    MainFragmentDirections.actionMainFragmentToEditAppointmentFragment(it.appointments[0].appointmentId)
                this.findNavController().navigate(action)
            },
            { locationMarkerAndAppointments, itemView ->
                val transition = itemView.background as TransitionDrawable
                transition.startTransition(200)
                appointmentViewModel.deleteAppointment(locationMarkerAndAppointments.appointments[0])
                createUndoSnackbar(itemView, locationMarkerAndAppointments.appointments[0])
                true
            }
        )

        var appointmentAndAppointments: List<AppointmentAndLocationMarker> = emptyList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        locationMarkerViewModel.locationMarkerAndAppointments.observe(this.viewLifecycleOwner) {
            appointmentAndAppointments = it
            submitFilteredLocationMarkersAndAppointments(adapter, it)
            /*appointments.let {
    locationMarkerAndAppointments = it
    adapter.submitList(locationMarkerAndAppointments)
}*/
        }

        binding.addNewAppointmentButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddAppointmentFragment()
            this.findNavController().navigate(action)
        }

        binding.checkBoxDone.setOnCheckedChangeListener { _, _ ->
            submitFilteredLocationMarkersAndAppointments(adapter, appointmentAndAppointments)
            adapter.notifyDataSetChanged()
        }
    }

    private fun submitFilteredLocationMarkersAndAppointments(
        adapter: LocationMarkerAndAppointmentListAdapter,
        appointmentAndAppointments: List<AppointmentAndLocationMarker>
    ) {
        adapter.submitList(
            appointmentAndAppointments.map {
                it.copy(appointments = filterAppointments(it.appointments))
            }
        )
    }

    private fun filterAppointments(appointments: List<Appointment>): List<Appointment> {
        if (!binding.checkBoxDone.isChecked) {
            return appointments.filter {
                !it.done
            }
        }
        return appointments
    }

    private fun createUndoSnackbar(itemView: View, appointment: Appointment) {
        Snackbar.make(
            binding.recyclerView as View,
            "Undo deleting ${appointment.name}",
            Snackbar.LENGTH_LONG
        )
            .setAction(
                "UNDO"
            ) {
                appointmentViewModel.addNewAppointment(appointment)
                val transition = itemView.background as TransitionDrawable
                transition.resetTransition()
                Toast.makeText(requireContext(), "${appointment.name} restored", Toast.LENGTH_SHORT)
                    .show()
            }
            .show()
    }
}
