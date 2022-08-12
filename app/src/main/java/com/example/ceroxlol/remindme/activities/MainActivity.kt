/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ceroxlol.remindme.activities

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.ceroxlol.remindme.BuildConfig
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.fragments.MainFragmentDirections
import com.example.ceroxlol.remindme.services.GpsTrackerService
import com.example.ceroxlol.remindme.services.LocationService
import com.example.ceroxlol.remindme.utils.Utils
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val TAG = this.javaClass.simpleName

    private lateinit var navController: NavController

    private var gpsTrackerService: GpsTrackerService? = null
    private var locationService: LocationService? = null

    // Tracks the bound state of the service.
    private var isServiceBound = false

    // Monitors connection to the while-in-use service.
    private val gpsTrackerServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as GpsTrackerService.LocalBinder
            gpsTrackerService = binder.service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            gpsTrackerService = null
        }
    }

    // Monitors the state of the connection to the service.
    private val locationServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationService.LocalBinder =
                service as LocationService.LocalBinder
            locationService = binder.service
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            isServiceBound = false
        }
    }


    //TODO: Create Introduction Screen
    //TODO: Setup Home
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(findViewById(R.id.toolbar))
        setupActionBarWithNavController(
            this,
            navController = navController,
            configuration = appBarConfiguration
        )

        /*// Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }*/

        /*gpsTrackerService = GpsTrackerService()

        val intent = Intent(this, GpsTrackerService::class.java)
        startService(intent)
        bindService(intent, gpsTrackerServiceConnection, Context.BIND_AUTO_CREATE)*/

    }

    override fun onStart() {
        super.onStart()
        val context = this
        PreferenceManager.getDefaultSharedPreferences(context)
        //.registerOnSharedPreferenceChangeListener(context)



        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    locationService!!.requestLocationUpdates()

                    // Bind to the service. If the service is in foreground mode, this signals to the service
                    // that since this activity is in the foreground, the service can exit foreground mode.
                    bindService(
                        Intent(this, LocationService::class.java), locationServiceConnection,
                        BIND_AUTO_CREATE
                    )
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            locationService!!.requestLocationUpdates()
        }

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(
            Intent(this, LocationService::class.java), locationServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        if (isServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(locationServiceConnection)
            isServiceBound = false
        }
        val context = this
        PreferenceManager.getDefaultSharedPreferences(context)
        //.unregisterOnSharedPreferenceChangeListener(context)
        super.onStop()
    }

    //TODO: Recheck permissions, on the phone I had to request in Pick Location
    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).toString()
        )
    }

    private fun requestPermissions(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            showInContextUI(...)
        }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.REQUESTED_PERMISSION)
            }
        }
    }

    /*//TODO: This should be handled in an introduction screen
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.mainFragment),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        }
    }*/

    /**
     * Handle navigation when the user chooses Up from the action bar.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(applicationContext, GpsTrackerService::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                locationService!!.requestLocationUpdates()
            } else {
                // Permission denied.
                Snackbar.make(
                    findViewById(R.id.mainFragment),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) { // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri: Uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

/*    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s == Utils.KEY_REQUESTING_LOCATION_UPDATES) {
            setButtonsState(
                sharedPreferences.getBoolean(
                    Utils.KEY_REQUESTING_LOCATION_UPDATES,
                    false
                )
            )
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            navController.navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}
