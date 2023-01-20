package com.example.ceroxlol.remindme.fragments

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
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
import com.example.ceroxlol.remindme.adapters.SearchResultAdapter
import com.example.ceroxlol.remindme.databinding.FragmentLocationBinding
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.utils.getHumanReadableAddress
import com.example.ceroxlol.remindme.utils.permissions.Permission
import com.example.ceroxlol.remindme.utils.permissions.PermissionManager
import com.example.ceroxlol.remindme.utils.toLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.maps.android.ui.IconGenerator
import java.util.*

const val NO_LOCATION_TO_EDIT = -10
private const val CLOSE_ZOOM = 18F
private const val TAG = "LocationFragment"

//TODO: Reiterate flow
class LocationFragment : Fragment() {

    private val permissionManager: PermissionManager = PermissionManager.from(this)

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val navigationArgs: LocationFragmentArgs by navArgs()

    private var mapIsReady = false
    private var locationMarkerIsReady = false

    private val _allReady = MutableLiveData(false)
    private val allReady: LiveData<Boolean> = _allReady

    private var locationMarker: LocationMarker? = null
    private var deviceLocation: Location? = null
    private lateinit var iconFactory: IconGenerator

    private val _addresses = MutableLiveData<List<Address>>()
    private val addresses: LiveData<List<Address>> = _addresses
    private var currentAddress: String = ""

    private val maxResults = 10

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val callback = OnMapReadyCallback { googleMap ->
        val mapView = childFragmentManager.findFragmentById(R.id.map)!!.view
        val locationButton = mapView!!.findViewById<ImageView>(Integer.parseInt("2"))
        val layoutParams = locationButton?.layoutParams as RelativeLayout.LayoutParams
        // position on right bottom
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        layoutParams.setMargins(0, 0, 30, 300)

        with(googleMap){
            map = this
            this.clear()

            this.uiSettings.isZoomControlsEnabled = true
            this.uiSettings.isMapToolbarEnabled = false

            this.setOnMapClickListener {
                this.clear()
                this.addMarker(
                    MarkerOptions().icon(
                        BitmapDescriptorFactory.fromBitmap(
                            iconFactory.makeIcon(
                                ""
                            )
                        )
                    ).position(it)
                )
                this.animateCamera(CameraUpdateFactory.newLatLngZoom(it, CLOSE_ZOOM))
                if(locationMarker == null){
                    locationMarker = LocationMarker(
                        location = DbLocation(it),
                        name = ""
                    )
                }
            }
        }

        mapIsReady = true
        updateReadiness()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getDeviceLocation()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        modifyMenuBar()

        iconFactory = IconGenerator(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        //Try loading the location from the database
        //If it's successful,
        locationMarkerViewModel.getLocationMarker(navigationArgs.locationMarkerId)
            .observe(this.viewLifecycleOwner) { locationMarker ->
                this.locationMarker = locationMarker

                locationMarker?.let {
                    moveCameraToLocationMarker()
                    map.addMarker(
                        MarkerOptions().icon(
                            BitmapDescriptorFactory.fromBitmap(
                                iconFactory.makeIcon(it.name)
                            )
                        ).position(it.location.toLatLng())
                    )
                }

                locationMarkerIsReady = true
                updateReadiness()
            }

        addresses.observe(this.viewLifecycleOwner) { addressList ->
            Log.d(
                LocationFragment::class.java.simpleName,
                "Received results. Amount: ${addressList.size}"
            )
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

        crateAllReadyObserver()
    }

    private fun modifyMenuBar() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.save_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_save_element -> {
                        if (locationMarker != null) {
                            showSaveDialog()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Please select a point on the map first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateReadiness() {
        _allReady.value =
            mapIsReady && locationMarkerIsReady
    }

    private fun crateAllReadyObserver() {
        allReady.observe(this.viewLifecycleOwner) { allIsPresent ->
            if (allIsPresent) {
                attemptMoveCamera()
                bindUi()
            }
        }
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
    }

    private fun bindUi() {
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
                            deviceLocation = task.result
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.")
                            Log.e(TAG, "Exception: %s", task.exception)
                        }
                    }
                }
            }
    }

    private fun attemptMoveCamera() {
        if (locationMarker != null) {
            moveCameraToLocationMarker()
        } else {
            moveCameraToDeviceLocation()
        }
    }

    private fun moveCameraToDeviceLocation() {
        deviceLocation?.let {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        it.latitude,
                        it.longitude
                    ),
                    CLOSE_ZOOM
                )
            )
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

    private fun showSaveDialog() {
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Add Location")
        alertDialog.setMessage("Add a name for your location:")

        val editTextLocationName =
            EditText(requireActivity()).also { it.setText(locationMarker!!.name) }
        alertDialog.setView(editTextLocationName)

        alertDialog.setPositiveButton("Save") { _, _ ->
            locationMarkerViewModel.upsertLocationMarker(
                locationMarker!!.copy(
                    name = editTextLocationName.text.toString()
                )
            )

            //Navigate back
            val navController = findNavController()
/*            if (navController.previousBackStackEntry?.id?.toInt() == R.id.appointmentFragment) {
                navController.previousBackStackEntry?.savedStateHandle?.set(
                    "locationMarker",
                    locationMarker!!.id
                )
            }*/
            findNavController().popBackStack()
        }

        alertDialog.setNegativeButton("Cancel", null)

        alertDialog.show()
    }
}