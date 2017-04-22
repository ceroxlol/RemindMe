package com.example.ceroxlol.remindme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //Initialize GPS Component
    private GPSTracker mGPSTracker;
    private Appointment[] mAppointments;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void init() {
        //Initial calls
        initData();

        //Init components
        initClasses();
    }

    private void initData() {
        //Right now, this method only creates a dummy Appointment, later it will read entries from the database
        this.mAppointments[0] = new Appointment(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance());
    }

    private void initClasses() {
        this.mGPSTracker = new GPSTracker(this);
        this.mAppointments = new Appointment[1];
        this.mAppointmentMetCheckingService = new AppointmentMetCheckingService(this.mGPSTracker, this.mAppointments);
        this.mAppointmentMetCheckingService.setRun(true);
        this.mAppointmentMetCheckingService.start();
    }

}
