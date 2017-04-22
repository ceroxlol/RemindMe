package com.example.ceroxlol.remindme;

import android.location.Location;

import java.util.Date;
import java.util.Calendar;

/**
 * Created by Ceroxlol on 20.04.2017.
 */

class Appointment {
    private enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time};
    private String mName, mAppointmentText;
    private Location mLocation;
    private Boolean mHasTime;
    private Calendar mAppointmentCreated, mAppointmentTime, mAppointmentRemindTime;
    private int mPriority;
    private mAppointmentType mType;
    //private picture

    //default
    public Appointment()
    {
    }

    //without time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Calendar appointmentCreated)
    {
        this.mType = mAppointmentType.values()[appointmentType];
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mHasTime = false;
    }

    //with time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Calendar appointmentCreated, Calendar appointmentTime, Calendar appointmentRemindTime)
    {
        this.mType = mAppointmentType.values()[appointmentType];
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mAppointmentTime = appointmentTime;
        this.mAppointmentRemindTime = appointmentRemindTime;
        this.mHasTime = true;
    }

    public void checkIfAppointmentIsMet(Location currentLocation) {

    }
}
