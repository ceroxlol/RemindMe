package com.example.ceroxlol.remindme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    //GPS Component
    private GPSTracker mGPSTracker;

    //Appointments
    private Appointment[] mAppointments;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;

    //Message window handler
    public Handler mHandler;

    //UI
    private LinearLayout mAppointmentLinearLayout;
    private Button mAppointmentAddNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        //Init components
        initClasses();

        //Initial calls
        initData();

        //Init UI
        initUI();
    }

    private void initUI() {
        fillAppointmentScrollView();
        this.mAppointmentAddNew.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddNewAppointment.class));
            }
        });
    }

    private void fillAppointmentScrollView()
    {
        //Add appointments to the ScrollView
        TextView appointment = new TextView(MainActivity.this);
        appointment.setText(this.mAppointments[0].getText());

        mAppointmentLinearLayout.addView(appointment);
    }

    private void initData() {
        //Right now, this method only creates a dummy Appointment, later it will read entries from the database
        this.mAppointments = new Appointment[1];
        this.mAppointments[0] = new Appointment(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance(), this);
    }

    private void initClasses() {
        //Init GPS Tracker
        this.mGPSTracker = new GPSTracker(this.getApplicationContext());

        //Init checker service thread for appointments met
        this.mAppointmentMetCheckingService = new AppointmentMetCheckingService(this.mGPSTracker, this.mAppointments, this);
        //this.mAppointmentMetCheckingService.setRun(true);
        this.mAppointmentMetCheckingService.start();

        //Init message handler for showing message on the main ui thread
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.
                alertView(message);
            }
        };

        //UI elements
        this.mAppointmentLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_appointment_list);
        this.mAppointmentAddNew = (Button) findViewById(R.id.button_add_new_appointment);
    }

    //Just for testing purposes
    public void alertView(Message message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle(message.getData().getString("name"))
                .setMessage(message.getData().getString("text"))
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
