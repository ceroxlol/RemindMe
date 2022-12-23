package com.example.ceroxlol.remindme.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerSpinnerAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAppointmentBinding
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.utils.isValidForPersistence
import com.example.ceroxlol.remindme.utils.toLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import java.time.Instant
import java.util.*

class AppointmentFragment : Fragment() {

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

    private var indexFromAppointmentAndLocationMarker = 0

    private val navigationArgs: AppointmentFragmentArgs by navArgs()
    private var navigationCameFromLocationMarkerAdded: Boolean = false

    //TODO
    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap

    private var mapIsReady = false
    private var locationsIsReady = false
    private var appointmentIsReady = false

    private var appointmentAndLocationMarker: AppointmentAndLocationMarker? = null
    private var locations: List<LocationMarker> = emptyList()

    private val _allReady = MutableLiveData(false)
    private val allReady: LiveData<Boolean> = _allReady

    private val pleaseAddNewLocationMarkerDummy = LocationMarker(
        id = -1,
        name = "Please add new Location!",
        location = DbLocation(LatLng(9.993682, 53.551086))
    )

    @SuppressLint("PotentialBehaviorOverride")
    private val callback = OnMapReadyCallback {
        map = it
        map.uiSettings.isMapToolbarEnabled = false

        map.setOnMarkerClickListener { marker ->
            val position = marker.tag as Int
            binding.appointmentLocation.setSelection(position)
            false
        }

        mapIsReady = true
        updateReadiness()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)

        //Init map
        with(binding.appointmentMap) {
            onCreate(savedInstanceState)
            //Make app available immediately
            onResume()
        }
        MapsInitializer.initialize(requireContext())
        binding.appointmentMap.getMapAsync(callback)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.appointment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                when(menuItem.itemId){
                    R.id.action_appointment_save ->{
                        saveAppointment()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        createObservers()

        // Move label to upper left corner
        binding.appointmentNameLabel.gravity = Gravity.TOP

        if (navigationArgs.appointmentId != -10) {
            createAppointmentAndLocationMarkerObserver()
            // If we came from the MainFragment and there is no appointment to load, it's ready
        } else {
            appointmentIsReady = true
            updateReadiness()
        }

        binding.appointmentAddLocation.setOnClickListener {
            val action =
                AppointmentFragmentDirections.actionAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }

        binding.appointmentLocation.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
                val location: LocationMarker = p0.getItemAtPosition(p2) as LocationMarker
                if (location.id == -1) return
                if (binding.appointmentLocation.isEnabled) {
                    moveToLocationOnMap(location)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //Do nothing
            }
        }

        binding.appointmentAddLocation.setOnClickListener {
            val action =
                AppointmentFragmentDirections.actionAppointmentFragmentToAddLocationFragment(NO_LOCATION_TO_EDIT)
            findNavController().navigate(action)
        }

    }

    private fun createAppointmentAndLocationMarkerObserver() {
        appointmentViewModel.appointmentAndLocationMarkerByAppointmentId(navigationArgs.appointmentId)
            .observe(this.viewLifecycleOwner) {
                if (it == null) {
                    //No entry for the appointment could be found. Signal this and return to main
                    navigateBackOnNonexistentAppointmentId()
                } else {
                    appointmentAndLocationMarker = it
                    //Bind appointment to UI
                    bind(it.appointment)

                    appointmentIsReady = true
                    updateReadiness()
                }
            }
    }

    private fun createLocationMarkerObserver() {

        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) {
            binding.appointmentLocation.isEnabled = it.isNotEmpty()

            binding.appointmentLocation.adapter = LocationMarkerSpinnerAdapter(
                requireContext(),
                it.ifEmpty { listOf(pleaseAddNewLocationMarkerDummy) }
            ).also { locationMarkerSpinnerAdapter ->
                locationMarkerSpinnerAdapter.setDropDownViewResource(R.layout.textview_spinner_locationmarker_singleitem)
            }

            locations = it

            locationsIsReady = true
            updateReadiness()
        }
    }

    private fun createObservers() {

        observeLocationMarkerWasAddedBackstackPopped()

        createLocationMarkerObserver()

        crateAllReadyObserver()

    }

    private fun updateReadiness() {
        _allReady.value =
            appointmentIsReady && mapIsReady && locationsIsReady
    }

    private fun navigateBackOnNonexistentAppointmentId() {

        Toast.makeText(
            requireContext(),
            "This appointment doesn't exist anymore",
            Toast.LENGTH_SHORT
        ).show()
        findNavController().popBackStack(R.id.mainFragment, true)

    }

    private fun bind(appointment: Appointment) {

        binding.apply {
            appointmentName.setText(appointment.name, TextView.BufferType.SPANNABLE)
        }

    }

    private fun crateAllReadyObserver() {

        allReady.observe(this.viewLifecycleOwner) { allIsPresent ->
            if (allIsPresent) {
                locations
                    .mapIndexedNotNull { index, locationMarker ->
                        index.takeIf { locationMarker.id == appointmentAndLocationMarker?.locationMarker?.id }
                    }
                    .firstOrNull()?.let {
                        indexFromAppointmentAndLocationMarker = it
                    }
                Log.i(
                    this.tag,
                    "indexFromAppointmentAndLocationMarker = $indexFromAppointmentAndLocationMarker"
                )

                populateMarkersOnMap()

                updateLocationSpinnerSelectedPosition()
            }
        }

    }

    private fun populateMarkersOnMap() {

        val iconFactory = IconGenerator(requireContext())
        locations.forEach {
            val marker = map.addMarker(
                MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(it.name)))
                    .position(it.location.toLatLng())
            )
            marker!!.tag = locations.indexOf(it)
        }

    }

    private fun observeLocationMarkerWasAddedBackstackPopped() {

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "locationMarkerAdded"
        )
            ?.observe(viewLifecycleOwner) {
                Log.d(
                    AppointmentFragment::class.java.simpleName,
                    "navigationCameFromLocationMarkerAdded = $it"
                )
                navigationCameFromLocationMarkerAdded = it
                updateLocationSpinnerSelectedPosition()
            }

    }

    private fun updateLocationSpinnerSelectedPosition() {

        val selectedPosition = if (navigationCameFromLocationMarkerAdded) {
            locations.size - 1
        } else {
            indexFromAppointmentAndLocationMarker
        }
        Log.d(this::class.java.simpleName, "selectedPosition = $selectedPosition")
        binding.appointmentLocation.setSelection(selectedPosition)

    }


    private fun moveToLocationOnMap(location: LocationMarker) =
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(location.location.toLatLng(), 15F)
        )


    private fun saveAppointment() {

        if (isAppointmentValid()) {
            val locationMarkerId = (binding.appointmentLocation.selectedItem as LocationMarker).id
            appointmentViewModel.saveAppointment(
                appointmentAndLocationMarker?.appointment?.copy(locationMarkerId = locationMarkerId)
                    ?: Appointment(
                        name = binding.appointmentName.text.toString(),
                        locationMarkerId = (binding.appointmentLocation.selectedItem as LocationMarker).id,
                        snooze = null,
                        created = Date.from(Instant.now()),
                        time = null
                    )
            )
            findNavController().popBackStack()
        } else if (!binding.appointmentLocation.isEnabled) {
            Toast.makeText(
                requireContext(),
                "No locations, please add one.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Please recheck, something's wrong.", Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun isAppointmentValid(): Boolean {
        return binding.appointmentName.text.toString().isValidForPersistence() &&
                (binding.appointmentLocation.selectedItem as LocationMarker).isValid()
    }

    override fun onResume() {
        super.onResume()
        binding.appointmentMap.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.appointmentMap.onDestroy()
    }

}