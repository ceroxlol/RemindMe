package com.example.ceroxlol.remindme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    //Initialize GPS Component
    private GPSTracker mGPSTracker;
    private Appointment[] mAppointments;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(Looper.getMainLooper()) {
            public void handleMessage(String name, String text) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                alertView(name, text);
            }
        };
        init();
    }

    private void init() {
        //Init components
        initClasses();

        //Initial calls
        initData();
    }

    private void initData() {
        //Right now, this method only creates a dummy Appointment, later it will read entries from the database
        //this.mAppointments[0] = new Appointment(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance(), this);
        //TextView temp = (TextView)findViewById(R.id.helloWorld);
        //temp.setText((int) this.mGPSTracker.getLongitude());
    }

    private void initClasses() {
        //Init GPS Tracker
        this.mGPSTracker = new GPSTracker(this.getApplicationContext());

        this.mAppointments = new Appointment[1];
        this.mAppointments[0] = new Appointment(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance(), this);
        this.mAppointmentMetCheckingService = new AppointmentMetCheckingService(this.mGPSTracker, this.mAppointments, this);
        this.mAppointmentMetCheckingService.setRun(true);
        this.mAppointmentMetCheckingService.start();
    }

    public void alertView(String name, String text) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());

        dialog.setTitle(name)
                .setMessage(text)
//  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialoginterface, int i) {
//          dialoginterface.cancel();
//          }})
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }
}
