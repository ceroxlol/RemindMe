package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationsSpinnerAdapter
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

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.addNewAppointment(
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker,
                null,
                false
            )
            val action =
                AddAppointmentFragmentDirections.actionAddAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        } else {
            Toast.makeText(
                requireContext(),
                "Please recheck, something's not correct.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return appointmentViewModel.isEntryValid(
            binding.appointmentName.text.toString(),
            binding.appointmentText.text.toString(),
            binding.appointmentLocation.selectedItem as LocationMarker
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locationMarkers ->
            locationMarkers.let {
                val adapter = LocationsSpinnerAdapter(requireContext(), it)

                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                binding.appointmentLocation.adapter = adapter
            }
        }

        binding.saveButton.setOnClickListener {
            if(locationsEmpty){
                Toast.makeText(requireContext(), "Please add locations first!", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }

}