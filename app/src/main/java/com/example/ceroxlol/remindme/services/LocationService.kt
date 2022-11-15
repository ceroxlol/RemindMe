/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ceroxlol.remindme.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.activities.MainActivity
import com.example.ceroxlol.remindme.fragments.AppointmentFragmentArgs
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.receiver.NotificationBroadcastReceiver
import com.example.ceroxlol.remindme.utils.AppDatabase
import com.example.ceroxlol.remindme.utils.Utils
import com.google.android.gms.location.*
import java.util.*


/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
class LocationService : LifecycleService() {
    private val mBinder: IBinder = LocalBinder()

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var mChangingConfiguration = false
    private var mNotificationManager: NotificationManager? = null

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private lateinit var mLocationRequest: LocationRequest

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var mLocationCallback: LocationCallback? = null
    private var mServiceHandler: Handler? = null

    /**
     * The current location.
     */
    private var mLocation: Location? = null

    private lateinit var database: AppDatabase
    private var appointments: List<AppointmentAndLocationMarker> = emptyList()
    private var formerlyNotifiedAppointments: List<AppointmentAndLocationMarker> = emptyList()

    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        createLocationRequest()
        lastLocation
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the channel for the notification
        val channel =
            NotificationChannel(FOREGROUND_SERVICE_RUNNING_CHANNEL_ID, "RemindMe Service", NotificationManager.IMPORTANCE_DEFAULT)

        // Set the Notification Channel for the Notification Manager.
        mNotificationManager!!.createNotificationChannel(channel)

        database = AppDatabase.getDatabase(applicationContext)
        database.appointmentDao().getAppointmentAndLocationMarkerNotDone().asLiveData()
            .observe(this) {
                Log.i(TAG, "added ${it.size} appointments to ${this.javaClass.simpleName}")
                appointments = it
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "Service started")
        val startedFromNotification = intent?.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification != null && startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
            mNotificationManager!!.cancelAll()
            return START_NOT_STICKY
        }
        // Tells the system to try to recreate the service after it has been killed.
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()")
        stopForeground(STOP_FOREGROUND_REMOVE)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(TAG, "Last client unbound from service")

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service")
            startForeground(NOTIFICATION_ID, serviceRunningNotification)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler!!.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        Utils.setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationService::class.java))
        try {
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback!!, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Utils.setRequestingLocationUpdates(this, false)
            Log.e(
                TAG,
                "Lost location permission. Could not request updates. $unlikely"
            )
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    private fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback!!)
            Utils.setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            Utils.setRequestingLocationUpdates(this, true)
            Log.e(
                TAG,
                "Lost location permission. Could not remove updates. $unlikely"
            )
        }
    }

    // Channel ID
    // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
    // The PendingIntent that leads to a call to onStartCommand() in this service.
    // The PendingIntent to launch activity.
    // Set the Channel ID for Android O.
    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private val serviceRunningNotification: Notification
        get() {
            val intent = Intent(this, LocationService::class.java)

            // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
            intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

            // The PendingIntent that leads to a call to onStartCommand() in this service.
            val servicePendingIntent = PendingIntent.getService(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val mainActivityIntent = Intent(this, MainActivity::class.java)

            val mainActivityPendingIntent = PendingIntent.getActivity(
                this, 0, mainActivityIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            return NotificationCompat.Builder(this, FOREGROUND_SERVICE_RUNNING_CHANNEL_ID)
                .addAction(
                    R.drawable.ic_cancel, getString(R.string.stop_locations),
                    servicePendingIntent
                )
                .setContentTitle(this.getString(R.string.running_in_background))
                .setContentIntent(mainActivityPendingIntent)
                .setGroup(locationServiceNotificationGroup)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setWhen(System.currentTimeMillis())
                .build()
        }

    private val lastLocation: Unit
        get() {
            try {
                mFusedLocationClient!!.lastLocation
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result != null) {
                            mLocation = task.result
                        } else {
                            Log.w(
                                TAG,
                                "Failed to get location."
                            )
                        }
                    }
            } catch (unlikely: SecurityException) {
                Log.e(
                    TAG,
                    "Lost location permission.$unlikely"
                )
            }
        }

    private fun onNewLocation(location: Location?) {
        Log.i(TAG, "New location: $location")
        mLocation = location

        val appointmentsToNotify =
            checkIfAppointmentsShouldCreateNotification(mLocation as Location)

        //Remove notifications for appointments that are no longer in range.
        formerlyNotifiedAppointments.minus(appointmentsToNotify.toSet()).forEach {
            mNotificationManager!!.cancel(it.appointment.id)
        }

        if (appointmentsToNotify.isNotEmpty()) {

            formerlyNotifiedAppointments = appointmentsToNotify

            Log.i(
                TAG,
                "Creating notifications for ${appointmentsToNotify.size} appointments"
            )

            appointmentsToNotify.forEach {
                mNotificationManager!!.notify(
                    it.appointment.id,
                    generateAppointmentNotification(it.appointment)
                )
            }
        }

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager!!.notify(
                NOTIFICATION_ID,
                serviceRunningNotification
            )
        }
    }

    private fun checkIfAppointmentsShouldCreateNotification(currentLocation: Location): List<AppointmentAndLocationMarker> {
        Log.d(TAG, "Filtering on ${appointments.size} appointments")
        val preferenceDistance = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt("appointment_update_distance", 50).toFloat()
        return appointments
            .filter { appointmentAndLocationMarker ->
                (appointmentAndLocationMarker.appointment.snooze == null
                        || appointmentAndLocationMarker.appointment.snooze.before(Calendar.getInstance().time))
                        && appointmentAndLocationMarker.locationMarker?.isValid() == true
                        && appointmentAndLocationMarker.locationMarker.isInRange(
                    currentLocation,
                    preferenceDistance
                )
            }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateAppointmentNotification(appointment: Appointment): Notification {
        Log.d(TAG, "generateNotification")

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val titleText = "Remember! \"${appointment.name}\""

        // 1. Create Notification Channel for O+ and beyond devices (26+).

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "RemindMe Appointments",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        // Adds NotificationChannel to system. Attempting to create an
        // existing notification channel with its original values performs
        // no operation, so it's safe to perform the below sequence.
        mNotificationManager!!.createNotificationChannel(notificationChannel)

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(titleText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val setAppointmentKTDoneIntent = Intent(
            this,
            NotificationBroadcastReceiver::class.java
        ).apply {
            action = "setDone"
            putExtra("appointmentId", appointment.id)
        }

        val appointmentDonePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            setAppointmentKTDoneIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val snoozeUntilNextTimeIntent = Intent(
            this,
            NotificationBroadcastReceiver::class.java
        ).apply {
            action = "setSnooze"
            putExtra("appointmentId", appointment.id)
        }

        val appointmentSnoozePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            snoozeUntilNextTimeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val editAppointmentNavDeepLink = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.appointmentFragment)
            .setArguments(AppointmentFragmentArgs.Builder(appointment.id).build().toBundle())
            .createPendingIntent()

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentIntent(editAppointmentNavDeepLink)
            .setGroup(appointmentNotificationGroup)
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

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
            this.interval = UPDATE_INTERVAL_IN_MILLISECONDS
            this.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The [Context].
     */
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            ACTIVITY_SERVICE
        ) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private const val PACKAGE_NAME =
            "com.google.android.gms.location.sample.locationupdatesforegroundservice"
        private val TAG = LocationService::class.java.simpleName

        /**
         * The name of the channel for notifications.
         */
        private const val FOREGROUND_SERVICE_RUNNING_CHANNEL_ID = "location_service_channel_01"
        private const val EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
                ".started_from_notification"

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        /**
         * The identifier for the notification displayed for the foreground service.
         */
        private const val NOTIFICATION_ID = 12345678

        const val NOTIFICATION_CHANNEL_ID = "LocationServiceAppointmentsChannel"

        private const val appointmentNotificationGroup = "AppointmentGroup"
        private const val locationServiceNotificationGroup = "LocationServiceGroup"
    }
}