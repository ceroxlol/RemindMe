package com.example.ceroxlol.remindme.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.example.ceroxlol.remindme.utils.AppDatabase

class AppointmentBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val database = context.let { AppDatabase.getDatabase(it) }
        val notificationManager = getSystemService(context, NotificationManager::class.java) as NotificationManager
        when (intent.getStringExtra("action")) {
            "setDone" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG,"No appointmentId passed")
                } else {
                    database.appointmentDao().setAppointmentDoneById(appointmentId)
                    notificationManager.cancel(appointmentId)
                }
            }
            "setSnooze" -> {
                val appointmentId = intent.getIntExtra("appointmentId", -1)
                if (appointmentId == -1) {
                    Log.e(TAG,"No appointmentId passed")
                } else {
                    notificationManager.cancel(appointmentId)
                }
            }
        }
    }

    companion object{
        const val TAG = "AppointmentBroadcastReceiver"
    }
}