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
import com.example.ceroxlol.remindme.models.AppointmentKT;
import com.example.ceroxlol.remindme.receiver.AppointmentActionReceiver;

import java.util.Calendar;

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
            for (AppointmentKT appointmentKT : mainActivity.getDb().appointmentDao().getAll()) {
                if (checkIfNotificationIsAlreadyShown(appointmentKT.getId()) && !appointmentKT.getDone())
                    closeNotification(appointmentKT.getId());
                if (checkIfAppointmentShouldBeShown(appointmentKT) && !checkIfNotificationIsAlreadyShown(appointmentKT.getId()))
                    showNotification(appointmentKT);
            }
            try {
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfAppointmentShouldBeShown(AppointmentKT appointmentKT) {
        return appointmentKT.getDone() &&
                checkIfAppointmentDistanceIsMet(appointmentKT, gpsTracker.getLocation()) &&
                checkIfAppointmentIsDue(appointmentKT);
    }

    private boolean checkIfAppointmentIsDue(AppointmentKT appointmentKT) {
        if (appointmentKT.getTime() == null)
            return true;
        return appointmentKT.getTime().compareTo(Calendar.getInstance().getTime()) <= 0;
    }

    private void closeNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.cancel(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkIfNotificationIsAlreadyShown(int appointmentId) {
        NotificationManager mNotificationManager = (NotificationManager) mainActivity.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == appointmentId) {
                return true;
            }
        }
        return false;
    }

    private void showNotification(AppointmentKT appointmentKT) {
        Intent intentActionSetInactive = new Intent(mainActivity.getApplicationContext(), AppointmentActionReceiver.class);

        intentActionSetInactive.putExtra("action", "setInactive");
        intentActionSetInactive.putExtra("appointmentId", appointmentKT.getId());

        Intent intentActionSnooze = new Intent(mainActivity.getApplicationContext(), AppointmentActionReceiver.class);

        intentActionSnooze.putExtra("action", "setSnooze");
        intentActionSnooze.putExtra("appointmentId", appointmentKT.getId());

        PendingIntent pIntentSetActive = PendingIntent.getBroadcast(mainActivity.getApplicationContext(), 1, intentActionSetInactive, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntentSnooze = PendingIntent.getBroadcast(mainActivity.getApplicationContext(), 1, intentActionSetInactive, PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO: On notification click, open a view of the appointment
        NotificationCompat.Builder mNotficationBuilder = new NotificationCompat.Builder(mainActivity.getApplicationContext(), channelId)
                //TODO: implement cool icons
                .setSmallIcon(R.drawable.amu_bubble_mask)
                .setContentTitle("Appointment '" + appointmentKT.getName() + "' is met")
                .setContentText(appointmentKT.getText())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "OK", pIntentSetActive))
                .addAction(new NotificationCompat.Action(R.drawable.amu_bubble_shadow, "Snooze 10 mins", pIntentSnooze));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity.getApplicationContext());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(appointmentKT.getId(), mNotficationBuilder.build());
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    private boolean checkIfAppointmentDistanceIsMet(AppointmentKT appointmentKT, Location currentLocation) {
        return appointmentKT.getLocation().getLocation().distanceTo(currentLocation) < 500;
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
