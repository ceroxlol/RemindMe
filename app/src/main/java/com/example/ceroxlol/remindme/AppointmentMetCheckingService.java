package com.example.ceroxlol.remindme;

import android.location.Location;

/**
 * Created by Ceroxlol on 22.04.2017.
 */

class AppointmentMetCheckingService extends Thread {
    private Appointment[] mAppointmentsToCheck;
    private GPSTracker mGPSTracker;
    private boolean run;
    //default ctor
    public AppointmentMetCheckingService()
    {

    }

    public AppointmentMetCheckingService(GPSTracker GPSTracker, Appointment[] appointmentsToCheck)
    {
        this.mGPSTracker = GPSTracker;
        this.mAppointmentsToCheck = appointmentsToCheck;
    }

    public void run() {
    while(run)
    {
        for (Appointment appointment: mAppointmentsToCheck
             ) {
            appointment.checkIfAppointmentIsMet(mGPSTracker.getLocation());
        }
    }
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
