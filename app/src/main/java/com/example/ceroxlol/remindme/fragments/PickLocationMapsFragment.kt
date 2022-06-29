package com.example.ceroxlol.remindme.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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

class PickLocationMapsFragment : Fragment() {

    private val permissionManager: PermissionManager = PermissionManager.from(this)

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationMarker: LocationMarker? = null
    private var lastKnownLocation: Location? = null

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private var _binding: FragmentPickLocationMapsBinding? = null
    private val binding get() = _binding!!

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        //Todo: set element to be the nearest senseful element
        map.setOnMapClickListener {
            map.clear()
            locationMarker = LocationMarker(
                location = DbLocation(it),
                name = ""
            )
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.svLocation.setOnQueryTextListener(
            object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()

                    if (queryName != "") {
                        val address =
                            Geocoder(requireActivity()).getFromLocationName(queryName, 1).first()
                        val latLng = LatLng(address.latitude, address.longitude)
                        map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(queryName)
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CLOSE_ZOOM))

                        locationMarker = LocationMarker(
                            location = DbLocation(latitude = latLng.latitude, longitude = latLng.longitude),
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
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                            )
                            map.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                } else {
                    getLocationPermission()
                }
            }
    }

    private fun moveCameraToLastKnownLocation() {
        lastKnownLocation?.let {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.latitude,
                        it.longitude
                    ), CLOSE_ZOOM
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

    private fun showSaveDialog() {
        if(locationMarker == null){
            Log.e(TAG, "locationMaker is null. This should not happen!")
            return
        }
        val editText = EditText(requireActivity()).also { it.setText(locationMarker!!.name) }
        val alertDialog = Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        alertDialog.setView(editText)

        alertDialog.setPositiveButton("Save") { p0, p1 ->
            locationMarkerViewModel.addNewLocationMarker(
                editText.text.toString(),
                locationMarker!!.location
            )

            //Navigate back
            val action =
                PickLocationMapsFragmentDirections.actionAddLocationFragmentToMainFragment()
            findNavController().navigate(action)
        }

        alertDialog.setNegativeButton("Cancel", null)

        alertDialog.show()
    }

    companion object{
        private val defaultLocation = LatLng(53.551086, 9.993682)
        private const val DEFAULT_ZOOM = 10F
        private const val CLOSE_ZOOM = 18F
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val TAG = "PickLocationMapsFragment"
    }
}