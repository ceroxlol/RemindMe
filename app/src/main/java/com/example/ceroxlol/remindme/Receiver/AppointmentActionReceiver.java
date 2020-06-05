package com.example.ceroxlol.remindme.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;
import android.util.Log;

import com.example.ceroxlol.remindme.MainActivity;

import java.util.Calendar;

import Data.Appointment;

public class AppointmentActionReceiver extends BroadcastReceiver {

    private String TAG = "AppBroadRec";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getStringExtra("action");
        int appointmentId = intent.getIntExtra("appointmentId", -1);
        if(action.equals("setInactive")){
            updateisActiveForAppointment(appointmentId);
            Toast.makeText(context,"Appointment done.",Toast.LENGTH_SHORT).show();
        }
        else if(action.equals("setSnooze")){
            setRemindTimer(appointmentId);
            Toast.makeText(context,"Appointment snoozed for 10 minutes.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.e(TAG, "Unkown Appointment Action received.");
            return;
        }
        closeNotification(context, appointmentId);

        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void updateisActiveForAppointment(int appointmentId){
        Appointment appointmentToUpdate = MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        appointmentToUpdate.setIsActive(false);
        MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().update(appointmentToUpdate);
        Log.i(TAG, "Received update isActive for appointment ID " + appointmentId);
    }

    public void setRemindTimer(int appointmentId){
        Appointment appointmentToUpdate = MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentToUpdate.getAppointmentTime());
        calendar.add(Calendar.MINUTE, 10);
        appointmentToUpdate.setAppointmentRemindTime(calendar.getTime());
        Log.i(TAG, "Set snooze timer for appointment to " + calendar.getTime());
    }

    public void closeNotification(Context context, int appointmentId){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.cancel(appointmentId);
    }
}