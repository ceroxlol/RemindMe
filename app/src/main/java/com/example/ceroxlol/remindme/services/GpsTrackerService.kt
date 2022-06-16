package com.example.ceroxlol.remindme.services

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.telecom.TelecomManager.EXTRA_LOCATION
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.activities.MainActivityKT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.*
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task


//https://github.com/googlecodelabs/while-in-use-location/blob/master/complete/src/main/java/com/example/android/whileinuselocation/ForegroundOnlyLocationService.kt
class GpsTrackerService : Service() {

    private val TAG = "GpsTrackerService"
    private val CHANNEL_ID = "ForegroundService Kotlin"

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val MIN_TIME_BW_UPDATES = (1000 * 60).toLong() // 1 minute
    private val STANDARD_TIME_BW_UPDATES = (1000 * 30).toLong() // 30 secs
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 50.toFloat() // 50 metres

    private val localBinder = LocalBinder()

    private var serviceRunningInForeground = false

    private lateinit var notificationManager: NotificationManager

    private var currentLocation: Location? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()

        setupForegroundTask()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(baseContext)

        getLastKnownLocation()

        setupLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        checkPermissions()

        val locationResult: Task<Location> = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(
            baseContext as Activity
        ) { task ->
            if (task.isSuccessful) {
                currentLocation = task.result
            } else {
                Log.d(TAG, "Current location is null. Using defaults.")
                Log.e(TAG, "Exception: %s", task.exception)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            val notification = generateNotification(currentLocation)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {
        checkPermissions()

        val locationRequest = create().apply {
            this.priority = LocationRequest.QUALITY_BALANCED_POWER_ACCURACY
            this.interval = MIN_TIME_BW_UPDATES
            this.fastestInterval = STANDARD_TIME_BW_UPDATES
            this.smallestDisplacement = MIN_DISTANCE_CHANGE_FOR_UPDATES
        }
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, currentLocation)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation)
                    )
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,

            )
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(baseContext, ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(baseContext, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                baseContext as Activity,
                arrayOf(android.Manifest.permission_group.LOCATION),
                1
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        setupForegroundTask()

        //stopSelf();
        return START_NOT_STICKY
    }

    private fun setupForegroundTask() {
        //do heavy work on a background thread
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivityKT::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service Kotlin Example")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: GpsTrackerService
            get() = this@GpsTrackerService
    }


    companion object {

        fun startService(context: Context) {
            val startIntent = Intent(context, GpsTrackerService::class.java)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, GpsTrackerService::class.java)
            context.stopService(stopIntent)
        }

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
    }
}