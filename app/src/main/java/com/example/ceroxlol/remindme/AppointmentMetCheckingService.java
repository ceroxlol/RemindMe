package com.example.ceroxlol.remindme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Message;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

class AppointmentMetCheckingService extends Thread {
    private Appointment[] mAppointmentsToCheck;
    private GPSTracker mGPSTracker;
    private boolean run;
    private Message mAppointMessage;

    private MainActivity mMainActivity;

    //default ctor
    public AppointmentMetCheckingService()
    {

    }

    public AppointmentMetCheckingService(GPSTracker GPSTracker, Appointment[] appointmentsToCheck, MainActivity mainActivity)
    {
        this.mGPSTracker = GPSTracker;
        this.mAppointmentsToCheck = appointmentsToCheck;
        this.mMainActivity = mainActivity;
        this.mAppointMessage = new Message();
    }

    public void run() {
        while(run)
        {
            for (Appointment appointment: mAppointmentsToCheck)
            {
                if(appointment.checkIfAppointmentDistanceIsMet(mGPSTracker.getLocation())) {
                    this.mMainActivity.alertView(appointment.getName(), appointment.getText());
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
