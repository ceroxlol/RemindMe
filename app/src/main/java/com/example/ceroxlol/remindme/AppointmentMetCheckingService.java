package com.example.ceroxlol.remindme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import Data.Appointment;

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

    public AppointmentMetCheckingService(GPSTracker GPSTracker, MainActivity mainActivity)
    {
        this.mGPSTracker = GPSTracker;
        this.mMainActivity = mainActivity;
        this.mAppointmentMessage = new Message();
        this.mMessageData = new Bundle();
        this.mChannelId = "AppointmentMetCheckingServiceChannelID";

        createNotificationChannel();
    }

    public void run() {
        while(run)
        {
            for (Appointment appointment : this.mMainActivity.getDBHelper().getAppointmentDaoRuntimeException().queryForAll())
            {
                if(checkIfAppointmentDistanceIsMet(appointment, mGPSTracker.getLocation())) {
                    this.mNotficationBuilder = new NotificationCompat.Builder(this.mMainActivity.getApplicationContext(), mChannelId)
                            .setContentTitle(appointment.getName())
                            .setContentText(appointment.getAppointmentText())
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.mMainActivity.getApplicationContext());

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(appointment.getId(), this.mNotficationBuilder.build());


                    mMessageData.putString("name", appointment.getName());
                    mMessageData.putString("text", appointment.getAppointmentText());

                    this.mAppointmentMessage.setData(this.mMessageData);
                    //Message toSend = this.mMainActivity.mMessageHandler.obtainMessage(1, this.mAppointmentMessage.obj);
                    Message toSend = this.mMainActivity.mMessageHandler.obtainMessage(1, "test");
                    toSend.sendToTarget();
                }
            }
            try {
                this.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    private boolean checkIfAppointmentDistanceIsMet(Appointment appointment, Location currentLocation) {
        if(appointment.getLocation().distanceTo(currentLocation) < 50)
            return true;
        return false;
    }

    private void createNotificationChannel() {
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
            notificationManager.createNotificationChannel(channel);
        }
    }
}
