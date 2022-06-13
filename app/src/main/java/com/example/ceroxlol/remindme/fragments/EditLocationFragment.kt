package com.example.ceroxlol.remindme.fragments

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentPickLocationMapsBinding
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.utils.permissions.PermissionManager
import com.example.ceroxlol.remindme.utils.toLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class EditLocationFragment : Fragment() {

    private val permissionManager = PermissionManager.from(this)

    private val viewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    private val navigationArgs: EditLocationFragmentArgs by navArgs()

    lateinit var locationMarker: LocationMarker
    lateinit var lastKnownLocation: Location

    private val defaultLocation = LatLng(53.551086, 9.993682)
    private val DEFAULT_ZOOM = 5

    private var _binding: FragmentPickLocationMapsBinding? = null
    private val binding get() = _binding!!

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        //Todo: set element to be the nearest senseful element
        map.setOnMapClickListener {
            map.clear()
            map.addMarker(MarkerOptions().position(it))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 10F))
        }

        getDeviceLocation()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickLocationMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        val id = navigationArgs.locationMarkerId
        // Retrieve the appointment details using the id.
        // Attach an observer on the data (instead of polling for changes) and only update the
        // the UI when the data actually changes.
        viewModel.retrieveLocationMarker(id).observe(this.viewLifecycleOwner) { selectedItem ->
            locationMarker = selectedItem
            bind(locationMarker)
        }
    }

    private fun bind(locationMarker: LocationMarker) {
        binding.apply {
            appointmentName.text = appointmentKT.name
            appointmentText.text = appointmentKT.text
            appointmentLocation.text = appointmentKT.location.name
            removeAppointment.setOnClickListener { showConfirmationDialog() }
        }
    }

    private fun getDeviceLocation() {
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.result
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    lastKnownLocation.toLatLng(), DEFAULT_ZOOM.toFloat()))
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                Log.d("EditLocation", "Current location is null. Using defaults.")
                Log.e("EditLocation", "Exception: %s", task.exception)
                map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        }
    }
}