package com.example.ceroxlol.remindme.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.ceroxlol.remindme.utils.AppDatabase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AppointmentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val database = context.let { AppDatabase.getDatabase(it) }
        val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
        when (intent.action) {
            "setDone" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG,"No appointmentId passed")
                } else {
                    Log.d(TAG, "Set done for appointmentId $appointmentId")
                    coroutineScope {

                    }
                    /*runBlocking {
                        database.appointmentDao().setAppointmentDoneById(appointmentId)
                    }*/
                    notificationManager.cancel(GPS_TRACKER_SERVICE_TAG, appointmentId + 10)
                }
            }
            "setSnooze" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG,"No appointmentId passed")
                } else {
                    Log.d(TAG, "Set snooze for appointmentId $appointmentId")
                    notificationManager.cancel(GPS_TRACKER_SERVICE_TAG, appointmentId)
                }
            }
        }
    }

    companion object{
        const val TAG = "AppointmentBroadcastReceiver"

        const val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES"

        const val GPS_TRACKER_SERVICE_TAG = "GpsTrackerService"
    }
}