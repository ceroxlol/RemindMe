package com.example.ceroxlol.remindme.fragments

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.SearchResultAdapter
import com.example.ceroxlol.remindme.databinding.FragmentAddLocationBinding
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
import java.util.*


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
    private var currentLatLng: LatLng? = null

    private val _addresses = MutableLiveData<List<Address>>()
    private val addresses: LiveData<List<Address>> = _addresses
    private var currentAddress: String = ""

    private val maxResults = 10

    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.clear()

        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false

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
            currentLatLng = it
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
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        locationMarkerViewModel.getLocationMarker(navigationArgs.locationMarkerId)
            .observe(this.viewLifecycleOwner) { locationMarker ->
                this.locationMarker = locationMarker

                if (locationMarker != null) {
                    //bind()
                    moveCameraToLocationMarker()
                    map.addMarker(MarkerOptions().position(this.locationMarker?.location!!.toLatLng()))
                }
            }

        addresses.observe(this.viewLifecycleOwner) { addressList ->
            Log.d(AddLocationFragment::class.java.simpleName, "Received results. Amount: ${addressList.size}")
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

        binding.saveButton.setOnClickListener {
            showSaveDialog()
        }

        binding.svLocation.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()

                    if (queryName != "") {
                        attemptInvokeGeocodeAddresses(queryName)
                        return true
                    }

                    return false
                }

                private fun attemptInvokeGeocodeAddresses(queryName: String) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        // declare here the geocodeListener, as it requires Android API 33
                        geocoder.getFromLocationName(queryName, maxResults) {
                            _addresses.value = it
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        _addresses.value =
                            geocoder.getFromLocationName(queryName, maxResults) as List<Address>
                    }
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val queryName = binding.svLocation.query.toString()
                    if (queryName.isBlank() || queryName == "") {
                        return false
                    }
                    attemptInvokeGeocodeAddresses(queryName)
                    return true
                }
            }
        )

        bind()
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

    private fun bind() {
        binding.apply {
            this.svLocation.setQuery(locationMarker?.name ?: "", false)
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
                        }
                        moveCameraToLocationMarker()
                    }
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
                }
            }
    }

    private fun showSaveDialog() {
        if (locationMarker == null) {
            Log.e(TAG, "locationMaker is null. This should not happen!")
            return
        }
        if (currentLatLng == null) {
            currentLatLng = locationMarker!!.location.toLatLng()
        }
        val locationId = locationMarker!!.id
        val editTextLocationName =
            EditText(requireActivity()).also { it.setText(locationMarker!!.name) }
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        alertDialog.setView(editTextLocationName)
        val locationName = editTextLocationName.text.toString()

        alertDialog.setPositiveButton("Save") { _, _ ->
            if (locationMarkerViewModel.isEntryValid(locationName, currentLatLng)) {
                locationMarkerViewModel.updateLocationMarker(
                    id = locationId,
                    name = locationName,
                    longitude = currentLatLng!!.longitude,
                    latitude = currentLatLng!!.latitude
                )

                //Navigate back
                val navController = findNavController()
                if (navController.previousBackStackEntry?.id?.toInt() == R.id.editAppointmentFragment) {
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "locationMarker",
                        locationId
                    )
                }
                findNavController().popBackStack()
            }
        }

        alertDialog.setNegativeButton("Cancel", null)

        alertDialog.show()
    }

    companion object {
        private const val CLOSE_ZOOM = 18F
        private const val TAG = "EditLocationFragment"
    }
}