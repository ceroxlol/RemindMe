package com.example.ceroxlol.remindme.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentPickLocationMapsBinding
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.utils.permissions.Permission
import com.example.ceroxlol.remindme.utils.permissions.PermissionManager
import com.example.ceroxlol.remindme.utils.toLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task


class EditLocationFragment : Fragment() {

    private val permissionManager: PermissionManager = PermissionManager.from(this)

    private val viewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val navigationArgs: EditLocationFragmentArgs by navArgs()

    private var locationMarker: LocationMarker? = null
    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(53.551086, 9.993682)
    private val DEFAULT_ZOOM = 5F
    private val CLOSE_ZOOM = 13F
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val TAG = "EditLocationFragment"

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

        updateLocationUI()

        getDeviceLocation()

        moveCameraToLastKnownLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
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
        viewModel.retrieveLocationMarker(id).observe(this.viewLifecycleOwner) { selectedItem ->
            locationMarker = selectedItem
            bind()
            moveCameraToLocationMarker()
            map.addMarker(MarkerOptions().position(locationMarker?.location!!.toLatLng()))
        }
    }

    private fun bind() {
        binding.apply {
            this.svLocation.setQuery(locationMarker!!.name, false)
        }
    }

    private fun getDeviceLocation() {
        permissionManager
            .request(Permission.Location)
            .rationale("Needs permission to access the location")
            .checkPermission { granted: Boolean ->
                if (granted) {
                    val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
                    locationResult.addOnCompleteListener(
                        requireActivity()
                    ) { task ->
                        if (task.isSuccessful) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.result
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            map.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                } else {
                    getLocationPermission()
                }
            }
    }

    private fun moveCameraToLocationMarker() {
        locationMarker?.let {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.location.latitude,
                        it.location.longitude
                    ), CLOSE_ZOOM
                )
            )
        }
    }

    private fun moveCameraToLastKnownLocation() {
        lastKnownLocation?.let {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.latitude,
                        it.longitude
                    ), DEFAULT_ZOOM
                )
            )
        }
    }

    private fun updateLocationUI() {
        permissionManager
            .request(Permission.Location)
            .rationale("Needs permission to access the location")
            .checkPermission { granted: Boolean ->
                if (granted) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    map.isMyLocationEnabled = false
                    map.uiSettings.isMyLocationButtonEnabled = false
                    getLocationPermission()
                }
            }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }
}