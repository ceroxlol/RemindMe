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
import androidx.preference.PreferenceManager
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
            appointments = it.toMutableList()
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
        //TODO: Remove after debugging
        //val locationUpdateDistance = PreferenceManager.getDefaultSharedPreferences(this).getInt("location_update_distance", 50).toFloat()
        locationRequest = LocationRequest.create().apply {
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            this.interval = MIN_TIME_BW_UPDATES
            this.fastestInterval = STANDARD_TIME_BW_UPDATES
            this.maxWaitTime = MAX_TIME_BW_UPDATES
            //this.smallestDisplacement = locationUpdateDistance
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                currentLocation = locationResult.lastLocation

                val appointmentsToNotify =
                    checkIfAppointmentsShouldNotify(currentLocation as Location)

                if (appointmentsToNotify.isNotEmpty()) {

                    Log.i(TAG, "Creating notifcations for ${appointmentsToNotify.size} appointments...")

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
        Log.d(TAG, "Filtering on ${appointments.size} appointments")
        val preferenceDistance = PreferenceManager.getDefaultSharedPreferences(this).getInt("appointment_update_distance", 50).toFloat()
        return appointments
            .filter { appointment ->
                !appointment.done && appointment.snooze?.before(Calendar.getInstance().time) == true && appointment.location?.isValid() == true
            }
            .filter { appointment ->
                Log.d(TAG, "Appointment ${appointment.id} is met!")
                val distance = FloatArray(1)
                Location.distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    appointment.location!!.location.latitude,
                    appointment.location.location.longitude,
                    distance
                )
                distance[0] < preferenceDistance
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
            .setSmallIcon(R.drawable.ic_notification)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(
                R.drawable.ic_notification,
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

        private val MIN_TIME_BW_UPDATES = TimeUnit.SECONDS.toMillis(5)
        private val STANDARD_TIME_BW_UPDATES = TimeUnit.SECONDS.toMillis(10)
        private val MAX_TIME_BW_UPDATES = TimeUnit.SECONDS.toMillis(30)
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 50f // 50 metres
    }
}