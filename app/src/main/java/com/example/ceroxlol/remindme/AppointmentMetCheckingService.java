package com.example.ceroxlol.remindme;

import android.location.Location;
import android.os.Bundle;
import android.os.Message;

import Data.Appointment;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

class AppointmentMetCheckingService extends Thread {
    private GPSTracker mGPSTracker;
    private boolean run;
    private Message mAppointmentMessage;
    private Bundle mMessageData;

    private MainActivity mMainActivity;

    //default ctor
    public AppointmentMetCheckingService()
    {

    }

    public AppointmentMetCheckingService(GPSTracker GPSTracker, MainActivity mainActivity)
    {
        this.mGPSTracker = GPSTracker;
        this.mMainActivity = mainActivity;
        this.mAppointmentMessage = new Message();
        this.mMessageData = new Bundle();
    }

    public void run() {
        while(run)
        {
            for (Appointment appointment : this.mMainActivity.getDBHelper().getDaoAppointmentRuntimeException().queryForAll())
            {
                if(checkIfAppointmentDistanceIsMet(appointment, mGPSTracker.getLocation())) {
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
}
