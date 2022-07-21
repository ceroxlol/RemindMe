package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationsSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentEditAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory

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

    private var _binding: FragmentEditAppointmentBinding? = null
    private val binding get() = _binding!!

    private var locationsEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAppointmentBinding.inflate(inflater, container, false)
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
                    locationsEmpty = it.isEmpty()
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
            if (locationsEmpty) {
                Toast.makeText(
                    requireContext(),
                    "No locations, please add one.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                saveAppointment()
            }
        }
    }

    private fun bind(appointment: Appointment) {
        binding.apply {
            appointmentName.setText(appointment.name, TextView.BufferType.SPANNABLE)
            appointmentText.setText(appointment.text, TextView.BufferType.SPANNABLE)
            saveButton.setOnClickListener { saveAppointment() }
        }
    }

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.updateAppointment(
                appointment.id,
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker
            )
            findNavController().popBackStack()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        if(locationsEmpty)
        {
            Toast.makeText(
                requireContext(),
                "No locations, please add one.",
                Toast.LENGTH_SHORT
            ).show()

            val action =
                AddAppointmentFragmentDirections.actionAddAppointmentFragmentToAddLocationFragment()
            findNavController().navigate(action)
        }
    }
}
