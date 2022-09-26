package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAddAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.google.android.gms.maps.model.LatLng


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

        binding.appointmentNameLabel.gravity = Gravity.TOP

        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locationMarkers ->
            locationMarkers.let { locationMarkerList ->
                locationsEmpty = locationMarkerList.isEmpty()
                if (!locationsEmpty) {
                    binding.appointmentLocation.adapter = LocationMarkerSpinnerAdapter(
                        requireContext(),
                        locationMarkerList
                    ).also { locationsSpinnerAdapter ->
                        locationsSpinnerAdapter.setDropDownViewResource(R.layout.textview_spinner_locationmarker_singleitem)
                    }
                    binding.appointmentLocation.isEnabled = true

                    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("key")
                        ?.observe(viewLifecycleOwner) {
                            val selectionPosition =
                                locationMarkerList.mapIndexedNotNull { index, locationMarker -> index.takeIf { locationMarker.id == it } }
                                    .first().or(0)
                            binding.appointmentLocation.setSelection(selectionPosition)
                        }

                } else {
                    binding.appointmentLocation.adapter = LocationMarkerSpinnerAdapter(
                        requireContext(), listOf(
                            LocationMarker(
                                id = -1,
                                name = "Please add new Location!",
                                location = DbLocation(LatLng(0.0, 0.0))
                            )
                        )
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