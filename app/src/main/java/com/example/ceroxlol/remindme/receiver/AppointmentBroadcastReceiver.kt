package com.example.ceroxlol.remindme.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import com.example.ceroxlol.remindme.utils.AppDatabase
import java.util.*

class AppointmentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val database = context.let { AppDatabase.getDatabase(it) }
        val notificationManager =
            getSystemService(context, NotificationManager::class.java) as NotificationManager
        when (intent.action) {
            "setDone" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG, "No appointmentId passed")
                } else {
                    Log.d(TAG, "Set done for appointmentId $appointmentId")
                    val pendingResult = goAsync()
                    Thread {
                        database.appointmentDao().setAppointmentDoneById(appointmentId)
                    }.start()
                    notificationManager.cancel(appointmentId)
                    pendingResult.finish()
                }
            }
            "setSnooze" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG, "No appointmentId passed")
                } else {
                    Log.d(TAG, "Set snooze for appointmentId $appointmentId")
                    val snoozeTimer =
                        PreferenceManager.getDefaultSharedPreferences(context).getString("snooze", "10")!!.toInt()
                    val pendingResult = goAsync()
                    Thread {
                        database.appointmentDao().setAppointmentSnooze(
                            appointmentId,
                            Calendar.getInstance().apply { this.add(Calendar.MINUTE, snoozeTimer) }.time
                        )
                    }.start()
                    notificationManager.cancel(appointmentId)
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val TAG = "AppointmentBroadcastReceiver"

        const val ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES"
    }
}
