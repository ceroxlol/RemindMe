package com.example.ceroxlol.remindme.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.ceroxlol.remindme.R;
import com.example.ceroxlol.remindme.models.LocationMarker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

public class ChooseLocationActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMarkerClickListener,
        OnMapClickListener {

    //TODO: Implement google places integration

    private static final String TAG = "CHOSE_LOCATION_ACTIVITY";
    private GoogleMap googleMap;
    private CameraPosition cameraPosition;
    private EditText editTextName;
    private List<LocationMarker> locationMarkers;
    private LatLng locationCoordinates;

    // The entry point to the Fused LocationHandler Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultCoordinates = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused LocationHandler Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        //TODO: Integrate Google maps suggestions for locations

        setContentView(R.layout.activity_add_new_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Taken from https://developers.google.com/maps/documentation/android-api/current-place-tutorial
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        editTextName = findViewById(R.id.editTextLocationName);
        Button buttonSave = findViewById(R.id.buttonSaveLocation);
        buttonSave.setOnClickListener(v -> {
            if (locationCoordinates != null) {
                String favoriteLocationName = editTextName.getText().toString();
                if (!favoriteLocationName.equals("")) {
                    saveNewFavoriteLocation(favoriteLocationName);
                    Toast.makeText(getApplicationContext(), "Location '" + favoriteLocationName + "' was saved.", Toast.LENGTH_SHORT).show();
                } else {
                    showAlertDialog("No name for new location",
                            "You have set no location name. Please set a name for the location in order to save it.");
                }
            } else {
                showAlertDialog("No marker was set",
                        "You have set no marker. Please set a marker before you save a new location.");
            }
        });

        //Get all available favorite locations
        //locationMarkers = getDb().locationMarkerDao().getAll();
    }

    private void saveNewFavoriteLocation(String favoriteLocationName) {
        Location location = new Location("");//provider name is unnecessary
        location.setLatitude(locationCoordinates.latitude);//your coords of course
        location.setLongitude(locationCoordinates.longitude);
        //Save any set Markers
        //LocationMarker locationMarker = new LocationMarker(0, location, favoriteLocationName);
        //getDb().locationMarkerDao().insert(locationMarker);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("new_favorite_location", lastKnownLocation);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing OK button
        alertDialog.setPositiveButton("OK", null);

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (googleMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        UiSettings uiSettings = this.googleMap.getUiSettings();

        // Create an instance of the UI elements for zooming
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true);

        this.googleMap.setOnMapClickListener(this);
        this.googleMap.setOnMarkerClickListener(this);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.layout_custom_info_contents,
                        findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My LocationHandler layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Set the saved Markers on the map
        loadSavedLocationsAsMarkers();
    }

    private void loadSavedLocationsAsMarkers() {
        if (locationMarkers.isEmpty())
            return;

        for (LocationMarker location : locationMarkers) {
            LatLng position = new LatLng(location.getLocation().getLatitude(), location.getLocation().getLongitude());
            IconGenerator iconFactory = new IconGenerator(this);
            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(location.getName()))).
                    position(position).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
            Marker marker = googleMap.addMarker(markerOptions);
            /*Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(location.getName()));*/
            //marker.showInfoWindow();
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation == null)
                            locationIsNullError(task.getException());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        locationIsNullError(task.getException());
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void locationIsNullError(Exception e) {
        Log.d(TAG, "Current location is null. Using defaults.");
        Log.e(TAG, "Exception: %s", e);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCoordinates, DEFAULT_ZOOM));
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        googleMap.clear();
        loadSavedLocationsAsMarkers();
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(latLng));
        locationCoordinates = marker.getPosition();
    }
}
