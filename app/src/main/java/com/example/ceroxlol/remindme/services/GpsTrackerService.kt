package com.example.ceroxlol.remindme.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.receiver.AppointmentBroadcastReceiver
import com.example.ceroxlol.remindme.utils.AppDatabase
import com.google.android.gms.location.*
import java.util.*
import java.util.concurrent.TimeUnit


class GpsTrackerService : LifecycleService() {

    /*
    * Checks whether the bound activity has really gone away (foreground service with notification
    * created) or simply orientation change (no-op).
    */
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    private lateinit var database: AppDatabase
    private lateinit var appointmentsKT: LiveData<List<Appointment>>
    private lateinit var appointments: MutableList<Appointment>
    private val appointmentNotificationDistance = 100

    private val localBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)

        setupLocationUpdates()

        getLastKnownLocation()

        database = AppDatabase.getDatabase(applicationContext)
        appointmentsKT = database.appointmentDao().getAll().asLiveData()

        appointments = mutableListOf()
        appointmentsKT.observe(this) {
            Log.i(TAG, "added ${it.size} appointments to ${this.javaClass.simpleName}")
            appointments.addAll(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                }
            }

    }


    @SuppressLint("MissingPermission")
    private fun setupLocationUpdates() {

        locationRequest = LocationRequest.create().apply {
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            this.interval = MIN_TIME_BW_UPDATES
            this.fastestInterval = STANDARD_TIME_BW_UPDATES
            this.maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            //TODO: Reuse once debugging is done
            //this.smallestDisplacement = MIN_DISTANCE_CHANGE_FOR_UPDATES
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                val appointmentsToNotify =
                    checkIfAppointmentsShouldNotify(currentLocation as Location)

                if (appointmentsToNotify.isNotEmpty()) {

                    Log.i(TAG, "found some appointments!")

/*                    val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                    intent.putExtra(EXTRA_LOCATION, currentLocation)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)*/

                    appointmentsToNotify.forEach {
                        notificationManager.notify(
                            TAG,
                            it.id,
                            generateNotification(it)
                        )
                    }
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!!
        )

    }

    private fun checkIfAppointmentsShouldNotify(currentLocation: Location): List<Appointment> {
        Log.i(TAG, "Filtering $appointments")
        return appointments
            //TODO: Remove once testing is done
            /*.filter { appointmentKT ->
                !appointmentKT.done && appointmentKT.snooze?.before(Calendar.getInstance().time) == true
            }*/
            .filter { appointmentKT ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    appointmentKT.location.location.latitude,
                    appointmentKT.location.location.longitude,
                    results
                )
                results[0] < appointmentNotificationDistance
            }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(appointment: Appointment): Notification {
        Log.d(TAG, "generateNotification()")

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val titleText = "Remember! " + appointment.name
        val mainNotificationText = "Note: " + appointment.text

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
            )

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val intentSetAppointmentKTDone = Intent(
            this,
            AppointmentBroadcastReceiver::class.java
        ).apply {
            action = "setDone"
            putExtra("appointmentId", appointment.id)
        }

        val appointmentDonePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intentSetAppointmentKTDone,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intentSnoozeUntilNextTime = Intent(
            this,
            AppointmentBroadcastReceiver::class.java
        ).apply {
            action = "setSnooze"
            putExtra("appointmentId", appointment.id)
        }

        val appointmentSnoozePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intentSnoozeUntilNextTime,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.amu_bubble_mask,
                "Ok",
                appointmentDonePendingIntent
            )
            .addAction(
                R.drawable.ic_cancel,
                "Snooze",
                appointmentSnoozePendingIntent
            )
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand")
        intent?.let { onTaskRemoved(it) }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Log.d(TAG, "onBind()")

        // become a background service.
        stopForeground(true)
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        startForeground(NOTIFICATION_ID, Notification())

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    inner class LocalBinder : Binder() {
        internal val service: GpsTrackerService
            get() = this@GpsTrackerService
    }

    companion object {

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        private const val NOTIFICATION_ID = 12345678

        const val NOTIFICATION_CHANNEL_ID = "GpsTrackerServiceChannel"

        private const val TAG = "GpsTrackerService"

        private const val MIN_TIME_BW_UPDATES = (1000 * 30).toLong() // 1 minute
        private const val STANDARD_TIME_BW_UPDATES = (1000 * 60).toLong() // 30 secs
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 50.toFloat() // 50 metres
    }
}