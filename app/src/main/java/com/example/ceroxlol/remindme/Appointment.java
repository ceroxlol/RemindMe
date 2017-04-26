package com.example.ceroxlol.remindme;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

    private MainActivity mMainActivity;

    //default
    public Appointment()
    {
    }

    //without time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Calendar appointmentCreated, MainActivity mainActivity)
    {
        this.mType = mAppointmentType.values()[appointmentType];
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mHasTime = false;
        this.mMainActivity = mainActivity;
    }

    //with time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Calendar appointmentCreated, Calendar appointmentTime, Calendar appointmentRemindTime, MainActivity mainActivity)
    {
        this.mType = mAppointmentType.values()[appointmentType];
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mAppointmentTime = appointmentTime;
        this.mAppointmentRemindTime = appointmentRemindTime;
        this.mHasTime = true;
        this.mMainActivity = mainActivity;
    }

    public boolean checkIfAppointmentDistanceIsMet(Location currentLocation) {
        if(this.mLocation.distanceTo(currentLocation) < 50)
            return true;
        return false;
    }

    public String getName() {
        return mName;
    }

    public String getText() {
        return mAppointmentText;
    }
}
