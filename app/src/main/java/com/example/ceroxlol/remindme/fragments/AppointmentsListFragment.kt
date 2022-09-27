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
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerAndAppointmentListAdapter
import com.example.ceroxlol.remindme.databinding.FragmentListAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentAndLocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentAndLocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.google.android.material.snackbar.Snackbar


class AppointmentsListFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by activityViewModels {
        AppointmentViewModelFactory(
            (activity?.application as RemindMeApplication).database.appointmentDao()
        )
    }

    private val appointmentAndLocationMarkerViewModel: AppointmentAndLocationMarkerViewModel by activityViewModels {
        AppointmentAndLocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
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
                    MainFragmentDirections.actionMainFragmentToEditAppointmentFragment(it.appointment.id)
                this.findNavController().navigate(action)
            },
            { appointmentAndLocationMarker, itemView ->
                val transition = itemView.findViewById<View>(R.id.appointment_name).background as TransitionDrawable
                transition.startTransition(200)
                appointmentViewModel.deleteAppointment(appointmentAndLocationMarker.appointment)
                createUndoSnackbar(itemView, appointmentAndLocationMarker.appointment)
                true
            }
        )

        var appointmentAndLocationMarkerList: List<AppointmentAndLocationMarker> = emptyList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        appointmentAndLocationMarkerViewModel.getAllSortedByLocationMarkerId.observe(this.viewLifecycleOwner) {
            appointmentAndLocationMarkerList = it
            submitFilteredLocationMarkersAndAppointments(adapter, it)
        }

        binding.addNewAppointmentButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddAppointmentFragment()
            this.findNavController().navigate(action)
        }

        binding.checkBoxDone.setOnCheckedChangeListener { _, _ ->
            submitFilteredLocationMarkersAndAppointments(adapter, appointmentAndLocationMarkerList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun submitFilteredLocationMarkersAndAppointments(
        adapter: LocationMarkerAndAppointmentListAdapter,
        locationMarkerAndAppointments: List<AppointmentAndLocationMarker>
    ) {
        adapter.submitList(
            locationMarkerAndAppointments.filter {
                !it.appointment.done
            }
        )
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
