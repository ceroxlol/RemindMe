package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAddAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory


class AddAppointmentFragment : Fragment() {

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

    lateinit var appointment: Appointment

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    private var locationsEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locationMarkers ->
            locationMarkers.let {
                locationsEmpty = it.isEmpty()
                if (!locationsEmpty) {
                    binding.appointmentLocation.adapter = LocationMarkerSpinnerAdapter(
                        requireContext(),
                        it
                    ).also { locationsSpinnerAdapter ->
                        locationsSpinnerAdapter.setDropDownViewResource(R.layout.textview_spinner_locationmarker_singleitem)
                    }
                    binding.appointmentLocation.isEnabled = true
                } else {
                    binding.appointmentLocation.adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        listOf("Please add a Location!")
                    )
                    binding.appointmentLocation.isEnabled = false
                }
            }
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

        binding.appointmentAddLocation.setOnClickListener {
            val action =
                AddAppointmentFragmentDirections.actionAddAppointmentFragmentToAddLocationFragment()
            findNavController().navigate(action)
        }
    }

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.addNewAppointment(
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker,
                null,
                false
            )
            findNavController().popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                "Please check, something's not correct.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isEntryValid(): Boolean {
        return appointmentViewModel.isEntryValid(
            binding.appointmentName.text.toString(),
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
}