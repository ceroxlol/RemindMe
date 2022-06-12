package com.example.ceroxlol.remindme.fragments

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentPickLocationMapsBinding
import com.example.ceroxlol.remindme.models.DbLocation
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

class PickLocationMapsFragment : Fragment() {

    private val permissionManager = PermissionManager.from(this)
    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap

    private lateinit var dbLocation: DbLocation
    private lateinit var locationName: String

    private val KEY_LOCATION = "location"
    private val defaultCoordinates = LatLng(-33.8523341, 151.2106085)

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
            locationName = ""
            dbLocation = DbLocation(it)
            map.addMarker(MarkerOptions().position(it))
        }

        setToCurrentPosition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
        }

        _binding = FragmentPickLocationMapsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // Taken from https://developers.google.com/maps/documentation/android-api/current-place-tutorial
        getDeviceLocation()

        binding.saveButton.setOnClickListener {
            showSaveDialog()

            val action =
                PickLocationMapsFragmentDirections.actionAddLocationFragmentToMainFragment()
            findNavController().navigate(action)
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
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))

                        dbLocation =
                            DbLocation(latitude = latLng.latitude, longitude = latLng.longitude)
                        locationName = queryName

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

    private fun setToCurrentPosition() {
        if(lastKnownLocation != null) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    lastKnownLocation!!.toLatLng(),
                    10F
                )
            )
        }
    }

    private fun getDeviceLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                permissionManager
                    .request(Permission.Location)
                    .rationale("Needs permission to access the location")
                    .checkPermission { granted: Boolean ->
                        if (granted) {
                            if (location == null) {
                                locationIsNullError()
                            } else {
                                lastKnownLocation = location
                            }
                        }
                    }
            }
    }

    private fun locationIsNullError() {
        Log.d("PickLocationMapsFragment", "Current location is null. Using fallback.")
        map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    lastKnownLocation?.toLatLng() ?: defaultCoordinates,
                    10F
                )
            )
        map.uiSettings.isMyLocationButtonEnabled = false
    }

    private fun showSaveDialog() {
        val editText = EditText(requireActivity()).also { it.setText(locationName) }
        val alertDialog = Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        alertDialog.setView(editText)

        alertDialog.setPositiveButton("Save") { p0, p1 ->
            locationMarkerViewModel.addNewLocationMarker(
                locationName,
                dbLocation
            )
        }

        alertDialog.setNegativeButton("Cancel", null)
    }
}