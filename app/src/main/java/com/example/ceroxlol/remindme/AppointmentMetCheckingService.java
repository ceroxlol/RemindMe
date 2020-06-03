package com.example.ceroxlol.remindme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.ceroxlol.remindme.Receiver.AppointmentAcknowledgedReceiver;

import java.util.Calendar;

import Data.Appointment;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

class AppointmentMetCheckingService extends Thread {
    private GPSTracker mGPSTracker;
    private boolean run;
    private Message mAppointmentMessage;
    private Bundle mMessageData;
    private String mChannelId;
    private NotificationCompat.Builder mNotficationBuilder;

    private MainActivity mMainActivity;

    public AppointmentMetCheckingService(GPSTracker GPSTracker, MainActivity mainActivity) {
        this.mGPSTracker = GPSTracker;
        this.mMainActivity = mainActivity;
        this.mAppointmentMessage = new Message();
        this.mMessageData = new Bundle();
        this.mChannelId = "AppointmentMetCheckingServiceChannelID";

        setUpNotificationChannel();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void run() {
        while (run) {
            for (Appointment appointment : this.mMainActivity.getDBHelper().getAppointmentDaoRuntimeException().queryForAll()) {
                if (checkIfAppointmentShouldBeShown(appointment))
                    showNotification(appointment);
                else
                    closeNotification(appointment.getId());
            }
            try {
                this.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfAppointmentShouldBeShown(Appointment appointment) {
        return !appointment.getAcknowledged() &&
                !checkIfNotificationIsAlreadyShown(appointment) &&
                checkIfAppointmentDistanceIsMet(appointment, mGPSTracker.getLocation()) &&
                checkIfAppointmentIsDue(appointment);
    }

    private boolean checkIfAppointmentIsDue(Appointment appointment) {
        //TODO: Workaround until I fixed the appointmentTimeIssue
        if(appointment.getAppointmentTime() == null)
            return false;
        return appointment.getAppointmentTime().compareTo(Calendar.getInstance().getTime()) > 0;
    }

    private void closeNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.mMainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.cancel(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfNotificationIsAlreadyShown(Appointment appointment) {
        NotificationManager mNotificationManager = (NotificationManager) this.mMainActivity.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == appointment.getId()) {
                return true;
            }
        }
        return false;
    }

    private void showNotification(Appointment appointment) {
        Intent intentActionAcknowledge = new Intent(mMainActivity.getApplicationContext(), AppointmentAcknowledgedReceiver.class);

        intentActionAcknowledge.putExtra("action", "setAcknowledge");
        intentActionAcknowledge.putExtra("appointmentId", appointment.getId());

        Intent intentActionSnooze = new Intent(mMainActivity.getApplicationContext(), AppointmentAcknowledgedReceiver.class);

        intentActionSnooze.putExtra("action", "setSnooze");
        intentActionSnooze.putExtra("appointmentId", appointment.getId());

        PendingIntent pIntentAcknowledge = PendingIntent.getBroadcast(mMainActivity.getApplicationContext(), 1, intentActionAcknowledge, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntentSnooze = PendingIntent.getBroadcast(mMainActivity.getApplicationContext(), 1, intentActionAcknowledge, PendingIntent.FLAG_UPDATE_CURRENT);

        this.mNotficationBuilder = new NotificationCompat.Builder(this.mMainActivity.getApplicationContext(), mChannelId)
                //TODO: implement cool items
                .setSmallIcon(R.drawable.amu_bubble_mask)
                .setContentTitle("Appointment '" + appointment.getName() + "' is met")
                .setContentText(appointment.getAppointmentText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "OK", pIntentAcknowledge))
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "Snooze", pIntentSnooze));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.mMainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(appointment.getId(), this.mNotficationBuilder.build());
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    private boolean checkIfAppointmentDistanceIsMet(Appointment appointment, Location currentLocation) {
        if (appointment.getLocation().distanceTo(currentLocation) < 500)
            return true;
        return false;
    }

    private void setUpNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ChannelName";
            String description = "ChannelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(mChannelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.mMainActivity.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
