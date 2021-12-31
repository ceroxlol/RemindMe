package com.example.ceroxlol.remindme.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationManagerCompat;
import android.widget.Toast;
import android.util.Log;

import com.example.ceroxlol.remindme.activities.MainActivity;

import java.util.Calendar;

import com.example.ceroxlol.remindme.models.Appointment;

public class AppointmentActionReceiver extends BroadcastReceiver {

    private final String TAG = "AppBroadRec";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        int appointmentId = intent.getIntExtra("appointmentId", -1);
        if (action.equals("setInactive")) {
            updateIsActiveForAppointment(appointmentId);
            Toast.makeText(context, "Appointment done.", Toast.LENGTH_SHORT).show();
        } else if (action.equals("setSnooze")) {
            setSnoozeForAppointmentTime(appointmentId);
            Toast.makeText(context, "Appointment snoozed for 10 minutes.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Unkown Appointment Action received.");
            return;
        }
        closeNotification(context, appointmentId);

        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void updateIsActiveForAppointment(int appointmentId) {
        Appointment appointmentToUpdate = MainActivity.databaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        appointmentToUpdate.setIsActive(false);
        MainActivity.databaseHelper.getAppointmentDaoRuntimeException().update(appointmentToUpdate);
        Log.i(TAG, "Received update isActive for appointment ID " + appointmentId);
    }

    public void setSnoozeForAppointmentTime(int appointmentId) {
        Appointment appointmentToUpdate = MainActivity.databaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentToUpdate.getAppointmentTime());
        calendar.add(Calendar.MINUTE, 10);
        appointmentToUpdate.setAppointmentTime(calendar.getTime());
        Log.i(TAG, "Set snooze timer for appointment to " + calendar.getTime());
    }

    public void closeNotification(Context context, int appointmentId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.cancel(appointmentId);
    }
}