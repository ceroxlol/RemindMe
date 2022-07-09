package com.example.ceroxlol.remindme.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentPickLocationMapsBinding
import com.example.ceroxlol.remindme.models.DbLocation
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

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
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

    private var _binding: FragmentPickLocationMapsBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = true

        val mapView = childFragmentManager.findFragmentById(R.id.map)!!.view
        val locationButton = mapView!!.findViewById<ImageView>(Integer.parseInt("2"))
        val layoutParams = locationButton?.layoutParams as RelativeLayout.LayoutParams
        // position on right bottom
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.setMargins(0, 0, 30, 300)

        map.setOnMapClickListener {
            map.clear()
            map.addMarker(MarkerOptions().position(it))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, CLOSE_ZOOM))
        }

        updateLocationUI()

        getDeviceLocation()
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        val id = navigationArgs.locationMarkerId
        locationMarkerViewModel.retrieveLocationMarker(id)
            .observe(this.viewLifecycleOwner) { selectedItem ->
                locationMarker = selectedItem
                bind()
                moveCameraToLocationMarker()
                map.addMarker(MarkerOptions().position(locationMarker?.location!!.toLatLng()))
            }

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.svLocation.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()

                    if (queryName != "") {
                        //TODO: show list of potential results
                        val address =
                            Geocoder(requireActivity()).getFromLocationName(queryName, 1)
                                .firstOrNull()
                        if (address == null) {
                            Toast.makeText(
                                requireContext(),
                                "No place found with this name", Toast.LENGTH_SHORT
                            ).show()
                            return false
                        }
                        val latLng = LatLng(address.latitude, address.longitude)
                        map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(queryName)
                        )
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                latLng,
                                CLOSE_ZOOM
                            )
                        )

                        locationMarker = LocationMarker(
                            location = DbLocation(
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            ),
                            name = queryName
                        )

                        return true
                    }

                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

            }
        )
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
                            moveCameraToLastKnownLocation()
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, CLOSE_ZOOM)
                            )
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
                    Log.i(TAG, "location permission granted")
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    map.isMyLocationEnabled = false
                    map.uiSettings.isMyLocationButtonEnabled = false
                    getLocationPermission()
                }
            }
    }

    //TODO: This should be handled in an introduction screen
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

    private fun showSaveDialog() {
        if (locationMarker == null) {
            Log.e(TAG, "locationMaker is null. This should not happen!")
            return
        }
        val editText = EditText(requireActivity()).also { it.setText(locationMarker!!.name) }
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        alertDialog.setView(editText)

        alertDialog.setPositiveButton("Save") { _, _ ->
            locationMarkerViewModel.addNewLocationMarker(
                editText.text.toString(),
                locationMarker!!.location
            )

            //Navigate back
            val action =
                EditAppointmentFragmentDirections.actionEditAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }

        alertDialog.setNegativeButton("Cancel", null)

        alertDialog.show()
    }

    companion object {
        private val defaultLocation = LatLng(9.993682, 53.551086)
        private const val DEFAULT_ZOOM = 13F
        private const val CLOSE_ZOOM = 18F
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val TAG = "EditLocationFragment"
    }
}