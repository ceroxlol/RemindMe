package com.example.ceroxlol.remindme.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ceroxlol.remindme.R;
import com.example.ceroxlol.remindme.activities.MainActivity;
import com.example.ceroxlol.remindme.receiver.AppointmentActionReceiver;

import java.util.Calendar;

import com.example.ceroxlol.remindme.models.Appointment;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

public class AppointmentMetCheckingService extends Thread {
    private final GpsTracker gpsTracker;
    private boolean run;
    private final String channelId;

    private final MainActivity mainActivity;

    public AppointmentMetCheckingService(GpsTracker gpsTracker, MainActivity mainActivity) {
        this.gpsTracker = gpsTracker;
        //TODO: Can't we get the stuff from the main activity by another way?
        this.mainActivity = mainActivity;
        channelId = "AppointmentMetCheckingServiceChannelID";

        setUpNotificationChannel();
    }

    public void run() {
        while (run) {
            for (Appointment appointment : mainActivity.getDBHelper().getAppointmentDaoRuntimeException().queryForAll()) {
                if (checkIfNotificationIsAlreadyShown(appointment) && !appointment.getIsActive())
                    closeNotification(appointment.getId());
                if (checkIfAppointmentShouldBeShown(appointment) && !checkIfNotificationIsAlreadyShown(appointment))
                    showNotification(appointment);
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfAppointmentShouldBeShown(Appointment appointment) {
        return appointment.getIsActive() &&
                checkIfAppointmentDistanceIsMet(appointment, gpsTracker.getLocation()) &&
                checkIfAppointmentIsDue(appointment);
    }

    private boolean checkIfAppointmentIsDue(Appointment appointment) {
        if (appointment.getAppointmentTime() == null)
            return true;
        return appointment.getAppointmentTime().compareTo(Calendar.getInstance().getTime()) <= 0;
    }

    private void closeNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.cancel(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfNotificationIsAlreadyShown(Appointment appointment) {
        NotificationManager mNotificationManager = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == appointment.getId()) {
                return true;
            }
        }
        return false;
    }

    private void showNotification(Appointment appointment) {
        Intent intentActionSetInactive = new Intent(mainActivity.getApplicationContext(), AppointmentActionReceiver.class);

        intentActionSetInactive.putExtra("action", "setInactive");
        intentActionSetInactive.putExtra("appointmentId", appointment.getId());

        Intent intentActionSnooze = new Intent(mainActivity.getApplicationContext(), AppointmentActionReceiver.class);

        intentActionSnooze.putExtra("action", "setSnooze");
        intentActionSnooze.putExtra("appointmentId", appointment.getId());

        PendingIntent pIntentSetActive = PendingIntent.getBroadcast(mainActivity.getApplicationContext(), 1, intentActionSetInactive, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntentSnooze = PendingIntent.getBroadcast(mainActivity.getApplicationContext(), 1, intentActionSetInactive, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO: On notification click, open a view of the appointment
        NotificationCompat.Builder mNotficationBuilder = new NotificationCompat.Builder(mainActivity.getApplicationContext(), channelId)
                //TODO: implement cool icons
                .setSmallIcon(R.drawable.amu_bubble_mask)
                .setContentTitle("Appointment '" + appointment.getName() + "' is met")
                .setContentText(appointment.getAppointmentText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "OK", pIntentSetActive))
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "Snooze 10 mins", pIntentSnooze));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(appointment.getId(), mNotficationBuilder.build());
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    private boolean checkIfAppointmentDistanceIsMet(Appointment appointment, Location currentLocation) {
        return appointment.getLocation().distanceTo(currentLocation) < 500;
    }

    private void setUpNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ChannelName";
            String description = "ChannelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.mainActivity.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }
}
