package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.example.ceroxlol.remindme.adapters.LocationMarkerSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentEditAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.google.android.gms.maps.model.LatLng

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

    private var _binding: FragmentEditAppointmentBinding? = null
    private val binding get() = _binding!!

    private var locationsEmpty = true

    private val addNewLocationMarker = LocationMarker(
        id = -1,
        name = "Please add new Location!",
        location = DbLocation(LatLng(0.0, 0.0))
    )

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

        //1. Setup appointment and UI elements --> AppointmentAndLocationMarker
        //2. Setup Location Spinner and its content --> LocationMarker
        //2a Set observer to when data is ready
        //2b Set currently selected item to AppointmentAndLocationMarker.locationMarkerId

        setLocationMarkerSpinnerAdapterContent()

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
                EditAppointmentFragmentDirections.actionEditAppointmentFragmentToAddLocationFragment()
            findNavController().navigate(action)
        }
    }

    private fun setLocationMarkerSpinnerAdapterContent() {
        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locationMarkerList ->
            locationsEmpty = locationMarkerList.isEmpty()
            // If there are no locations, we add a dummy
            if (locationsEmpty) {
                addNewLocationMarkerToEmptyList(locationMarkerList)
            }
            setLocationMarkerSpinnerAdapterContent(locationMarkerList)

            setLocationMarkerSpinnerSelection(locationMarkerList)
        }
    }

    private fun addNewLocationMarkerToEmptyList(locationMarkerList: List<LocationMarker>) {
        (locationMarkerList as MutableList).add(
            0,
            addNewLocationMarker
        )
        binding.appointmentLocation.isEnabled = false
    }

    private fun setLocationMarkerSpinnerSelection(locationMarkerList: List<LocationMarker>) {
        appointmentViewModel.appointmentAndLocationMarkerByAppointmentId(navigationArgs.appointmentId)
            .observe(this.viewLifecycleOwner) { appointmentAndLocationMarker ->
                if (appointmentAndLocationMarker != null) {
                    //In case we come here from the "AddLocation" we'll want to set the currently selected element in the spinner
                    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
                        "locationMarkerAdded"
                    )
                        ?.observe(viewLifecycleOwner) { locationMarkerAdded ->
                            Log.i(
                                EditAppointmentFragment::class.java.simpleName,
                                "I am here and locationMarkerAdded is $locationMarkerAdded"
                            )
                            setSelectionForLocationMarkerSpinner(
                                locationMarkerAdded,
                                appointmentAndLocationMarker,
                                locationMarkerList
                            )
                        }
                    binding.appointmentLocation.isEnabled = true

                    bind(appointmentAndLocationMarker.appointment)
                } else {
                    //No entry for the appointment could be found. Signal this and return to main
                    Toast.makeText(
                        requireContext(),
                        "This appointment doesn't exist anymore",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack(R.id.mainFragment, true)
                }
            }
    }

    private fun setSelectionForLocationMarkerSpinner(
        locationMarkerAdded: Boolean,
        appointmentAndLocationMarker: AppointmentAndLocationMarker,
        locationMarkerList: List<LocationMarker>
    ) {
        if (locationMarkerAdded) {
            binding.appointmentLocation.setSelection(locationMarkerList.size - 1)
        } else {
            val selectionPosition =
                locationMarkerList.mapIndexedNotNull { index, locationMarker -> index.takeIf { locationMarker.id == appointmentAndLocationMarker.locationMarker?.id } }
                    .firstOrNull()
            selectionPosition?.let {
                binding.appointmentLocation.setSelection(
                    it
                )
            }
        }
    }

    private fun setLocationMarkerSpinnerAdapterContent(locationMarkerList: List<LocationMarker>) {
        val adapter = LocationMarkerSpinnerAdapter(requireContext(), locationMarkerList)
        adapter.setDropDownViewResource(R.layout.textview_spinner_locationmarker_singleitem)
        binding.appointmentLocation.adapter = adapter
    }

    private fun bind(appointment: Appointment) {
        binding.apply {
            appointmentName.setText(appointment.name, TextView.BufferType.SPANNABLE)
            saveButton.setOnClickListener { saveAppointment() }
        }
    }

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.updateAppointment(
                navigationArgs.appointmentId,
                binding.appointmentName.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker
            )
            findNavController().popBackStack()
        } else {
            Toast.makeText(
                requireContext(),
                "Please recheck, something's not correct.", Toast.LENGTH_SHORT
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
