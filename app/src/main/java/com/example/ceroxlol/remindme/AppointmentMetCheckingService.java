package com.example.ceroxlol.remindme;

import android.os.Bundle;
import android.os.Message;

import DataHandler.AppointmentHandler;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

class AppointmentMetCheckingService extends Thread {
    private AppointmentHandler[] mAppointmentsToCheck;
    private GPSTracker mGPSTracker;
    private boolean run;
    private Message mAppointmentMessage;
    private Bundle mMessageData;

    private MainActivity mMainActivity;

    //default ctor
    public AppointmentMetCheckingService()
    {

    }

    public AppointmentMetCheckingService(GPSTracker GPSTracker, AppointmentHandler[] appointmentsToCheck, MainActivity mainActivity)
    {
        this.mGPSTracker = GPSTracker;
        this.mAppointmentsToCheck = appointmentsToCheck;
        this.mMainActivity = mainActivity;
        this.mAppointmentMessage = new Message();
        this.mMessageData = new Bundle();
    }

    public void run() {
        while(run)
        {
            for (AppointmentHandler appointmentHandler : mAppointmentsToCheck)
            {
                if(appointmentHandler.checkIfAppointmentDistanceIsMet(mGPSTracker.getLocation())) {
                    mMessageData.putString("name", appointmentHandler.mAppointment.getName());
                    mMessageData.putString("text", appointmentHandler.mAppointment.getAppointmentText());

                    this.mAppointmentMessage.setData(this.mMessageData);
                    //Message toSend = this.mMainActivity.mHandler.obtainMessage(1, this.mAppointmentMessage.obj);
                    Message toSend = this.mMainActivity.mHandler.obtainMessage(1, "test");
                    toSend.sendToTarget();
                }
            }
            try {
                this.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
