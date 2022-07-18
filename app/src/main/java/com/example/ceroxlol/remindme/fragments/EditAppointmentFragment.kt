package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationsSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAppointmentDetailBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditAppointmentFragment : Fragment() {

    private val appointmentViewModel: AppointmentViewModel by activityViewModels {
        AppointmentViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private val navigationArgs: EditAppointmentFragmentArgs by navArgs()

    lateinit var appointment: Appointment

    private var _binding: FragmentAppointmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.appointmentId
        // Retrieve the appointment details using the id.
        // Attach an observer on the data (instead of polling for changes) and only update the
        // the UI when the data actually changes.
        appointmentViewModel.retrieveAppointment(id).observe(this.viewLifecycleOwner) { selectedItem ->
            appointment = selectedItem

            locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locationMarkers ->
                locationMarkers.let {
                    val adapter = LocationsSpinnerAdapter(requireContext(), it)

                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                    binding.appointmentLocation.adapter = adapter

                    val selectionPosition = it.mapIndexedNotNull{index, locationMarker ->  index.takeIf { locationMarker.id == appointment.location?.id }}.first().or(0)
                    binding.appointmentLocation.setSelection(selectionPosition)
                }
            }

            bind(appointment)
        }

        binding.saveButton.setOnClickListener {
            saveAppointment()
        }
    }

    private fun bind(appointment: Appointment) {
        binding.apply {
            appointmentName.setText(appointment.name, TextView.BufferType.SPANNABLE)
            appointmentText.setText(appointment.text, TextView.BufferType.SPANNABLE)
            saveButton.setOnClickListener { saveAppointment() }
            //removeAppointment.setOnClickListener { showDeletionDialog() }
        }
    }

    private fun showDeletionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteItem()
            }
            .show()
    }

    private fun deleteItem() {
        appointmentViewModel.deleteAppointment(appointment)
        findNavController().navigateUp()
    }

    /**
     * Called when fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.updateAppointment(
                appointment.id,
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker
            )
            val action =
                EditAppointmentFragmentDirections.actionEditAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }
        else{
            Toast.makeText(
                requireContext(),
                "Please recheck, something's not correct.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isEntryValid(): Boolean {
        return appointmentViewModel.isEntryValid(
            binding.appointmentName.text.toString(),
            binding.appointmentText.text.toString(),
            binding.appointmentLocation.selectedItem as LocationMarker
        )
    }
}
