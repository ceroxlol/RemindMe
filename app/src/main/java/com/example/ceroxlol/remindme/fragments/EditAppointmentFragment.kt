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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    private var appointmentAndLocationMarker: AppointmentAndLocationMarker? = null
    private var locationMarkers: List<LocationMarker> = emptyList()
    private var navigationCameFromLocationMarkerAdded: Boolean = false

    private val _fetchedAllData = MutableLiveData(false)
    private val fetchedAllData : LiveData<Boolean> = _fetchedAllData

    private val pleaseAddNewLocationMarkerDummy = LocationMarker(
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

        observeLocationMarkerAdded()

        //1. Setup appointment and UI elements --> AppointmentAndLocationMarker
        //2. Setup Location Spinner and its content --> LocationMarker
        //2a Set observer to when data is ready
        //2b Set currently selected item to AppointmentAndLocationMarker.locationMarkerId

        setupAppointmentUiElements()

        setupLocationSpinner()

        setupLocationSelectionObserver()

        binding.saveButton.setOnClickListener {
            if (locationMarkers.isEmpty()) {
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

    private fun setupLocationSelectionObserver() {
        fetchedAllData.observe(this.viewLifecycleOwner){ allDataPresent ->
            if (allDataPresent){
                setLocationMarkerSpinnerSelection()
            }
        }
    }

    private fun setupLocationSpinner() {
        //2. Setup Location Spinner and its content --> LocationMarker
        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) {
            locationMarkers = it
            // If there are no locations, we add a dummy
            if (locationMarkers.isEmpty()) {
                addNewLocationMarkerToEmptyList(locationMarkers)
            }

            val adapter = LocationMarkerSpinnerAdapter(requireContext(), locationMarkers)
            adapter.setDropDownViewResource(R.layout.textview_spinner_locationmarker_singleitem)
            binding.appointmentLocation.adapter = adapter

            if(appointmentAndLocationMarker != null){
                _fetchedAllData.value = true
            }
        }
    }

    private fun setupAppointmentUiElements() {
        //1. Setup appointment and UI elements --> AppointmentAndLocationMarker
        appointmentViewModel.appointmentAndLocationMarkerByAppointmentId(navigationArgs.appointmentId)
            .observe(this.viewLifecycleOwner){
                appointmentAndLocationMarker = it
                if (appointmentAndLocationMarker == null){
                    //No entry for the appointment could be found. Signal this and return to main
                    navigateBackOnNonexistentAppointmentId()
                } else {
                    //Bind appointment to UI
                    binding.appointmentLocation.isEnabled = true
                    bind(appointmentAndLocationMarker!!.appointment)
                }

                if(locationMarkers.isNotEmpty()){
                    _fetchedAllData.value = true
                }
            }
    }

    private fun observeLocationMarkerAdded(){
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "locationMarkerAdded"
        )
            ?.observe(viewLifecycleOwner) {
                Log.d(EditAppointmentFragment::class.java.simpleName, "navigationCameFromLocationMarkerAdded = $it")
                navigationCameFromLocationMarkerAdded = it
            }
    }

    private fun addNewLocationMarkerToEmptyList(locationMarkerList: List<LocationMarker>) {
        (locationMarkerList as MutableList).add(
            0,
            pleaseAddNewLocationMarkerDummy
        )
        binding.appointmentLocation.isEnabled = false
    }

    private fun navigateBackOnNonexistentAppointmentId() {
        Toast.makeText(
            requireContext(),
            "This appointment doesn't exist anymore",
            Toast.LENGTH_SHORT
        ).show()
        findNavController().popBackStack(R.id.mainFragment, true)
    }

    private fun setLocationMarkerSpinnerSelection() {
        if (navigationCameFromLocationMarkerAdded) {
            binding.appointmentLocation.setSelection(locationMarkers.size - 1)
        } else {
            val selectionPosition =
                locationMarkers.mapIndexedNotNull { index, locationMarker -> index.takeIf { locationMarker.id == appointmentAndLocationMarker?.locationMarker?.id } }
                    .firstOrNull()
            Log.i(this.tag, "selectPositon is $selectionPosition")
            selectionPosition?.let {
                binding.appointmentLocation.setSelection(
                    it
                )
            }
        }
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
