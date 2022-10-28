package com.example.ceroxlol.remindme.fragments

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.SearchResultAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAddLocationBinding
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.utils.isValidForPersistence
import com.example.ceroxlol.remindme.utils.permissions.Permission
import com.example.ceroxlol.remindme.utils.permissions.PermissionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import java.util.*

class AddLocationFragment : Fragment() {

    private val permissionManager: PermissionManager = PermissionManager.from(this)

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationMarker: LocationMarker? = null
    private var lastKnownLocation: Location? = null

    private val _addresses = MutableLiveData<List<Address>>()
    private val addresses: LiveData<List<Address>> = _addresses
    private var currentAddress: String = ""

    private val maxResults = 10

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        //TODO: For debugging only
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false


        val mapView = childFragmentManager.findFragmentById(R.id.map)!!.view
        val locationButton = mapView!!.findViewById<ImageView>(Integer.parseInt("2"))
        val layoutParams = locationButton?.layoutParams as RelativeLayout.LayoutParams
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.setMargins(0, 0, 30, 300)

        map.setOnMapClickListener {
            map.clear()
            locationMarker = LocationMarker(
                location = DbLocation(it),
                name = "",
                address = null
            )
            map.addMarker(MarkerOptions().position(it))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, CLOSE_ZOOM))
        }

        updateLocationUI()

        getDeviceLocation()
    }

    //TODO: Set Home location
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
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        addresses.observe(this.viewLifecycleOwner) { addressList ->
            Log.d(AddLocationFragment::class.java.simpleName, "Received search results. Amount: ${addressList.size}")
            val adapter = SearchResultAdapter(
                requireContext(),
                addressList
            ) {
                currentAddress = it.getHumanReadableAddress()
                navigateTowardsSelectedAddress(it)
                clearAdapterData()
            }
            binding.svLocationResults.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        val geocoder = Geocoder(requireActivity(), Locale.GERMANY)

        binding.svLocation.queryHint = "Search for a location..."
        binding.svLocation.setOnQueryTextListener(
            object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()

                    if (queryName != "") {
                        attemptInvokeGeocodeAddresses(queryName)
                        return true
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()

                    if (queryName.isBlank() || queryName == "") {
                        return false
                    }
                    attemptInvokeGeocodeAddresses(queryName)
                    return true
                }

                private fun attemptInvokeGeocodeAddresses(queryName: String) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        // declare here the geocodeListener, as it requires Android API 33
                        geocoder.getFromLocationName(queryName, maxResults) {
                            _addresses.postValue(it)
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        _addresses.value =
                            geocoder.getFromLocationName(queryName, maxResults) as List<Address>
                    }
                }
            }
        )
    }

    private fun clearAdapterData() {
        binding.svLocationResults.adapter = null
    }

    private fun navigateTowardsSelectedAddress(address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        map.clear()
        map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(address.getHumanReadableAddress())
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
            name = address.getHumanReadableAddress()
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
                            updateLocationUI()
                            moveCameraToLastKnownLocation()
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                            map.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
                            )
                            updateLocationUI()
                        }
                    }
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
                    map.setLocationEnabled(true)
                } else {
                    map.setLocationEnabled(false)
                }
            }
    }

    //TODO: Show better looking dialogue...
    private fun showSaveDialog() {
        if (locationMarker == null) {
            Log.e(TAG, "locationMaker is null. This should not happen!")
            return
        }
        val editText = EditText(requireActivity()).also { it.setText(locationMarker!!.name) }
        val alertDialog = Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        alertDialog.setView(editText)

        alertDialog.setPositiveButton("Save") { _, _ ->
            if(!editText.text.toString().isValidForPersistence()){
                Toast.makeText(requireContext(), "Please add a name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            locationMarkerViewModel.addNewLocationMarker(
                editText.text.toString(),
                locationMarker!!.location
            )

            //Navigate back
            val navController = findNavController()
            navController.previousBackStackEntry?.savedStateHandle?.set("locationMarkerAdded", true)
            navController.popBackStack()
        }

        alertDialog.setNegativeButton("Cancel", null)

        alertDialog.show()
    }

    companion object {
        private val defaultLocation = LatLng(9.993682, 53.551086)
        private const val DEFAULT_ZOOM = 10F
        private const val CLOSE_ZOOM = 18F
        private const val TAG = "PickLocationMapsFragment"
    }
}

//TODO: Set the correct address here
fun Address.getHumanReadableAddress(): String {
    return this.featureName + ", " + this.adminArea + ", " + this.countryName
}

@SuppressLint("MissingPermission")
fun GoogleMap.setLocationEnabled(enabled: Boolean){
    this.isMyLocationEnabled = enabled
    this.uiSettings.isMyLocationButtonEnabled = enabled
}