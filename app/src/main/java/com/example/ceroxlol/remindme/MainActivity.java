package com.example.ceroxlol.remindme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import Data.FavoriteLocation;
import DataHandler.Appointment;
import DatabaseServices.DatabaseHelper;
import GUI.AppointmentsAdapter;

public class MainActivity extends AppCompatActivity {

    //PUBLIC
    //Message window handler
    public Handler mHandler;
    //Enum for Type
    public static enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time};
    //Database
    //private DBHelper mDBHelper;
    public static DatabaseHelper mDatabaseHelper = null;

    //PRIVATE
    //GPS Component
    private GPSTracker mGPSTracker;

    //Appointments
    private Appointment[] mAppointments;
    private ArrayList<Data.Appointment> mAppointmentList;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;

    //UI
    private LinearLayout mAppointmentLinearLayout;
    private Button mAppointmentAddNew;
    private Button mLocationAddNew;
    private Button mLocationEdit;
    private ArrayAdapter<Data.Appointment> mAppointmentArrayAdapter;

    //Requests
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final int REQUEST_NEW_FAVORITE_LOCATION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Call program setup
        init();
    }


    private void init() {

        //Permission Check
        checkPermissions();

        //Init components
        initClasses();

        //Initial calls
        initData();

        //Init UI
        initUI();
    }

    private void checkPermissions() {
        //Check Storage permission for database purposes
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void initUI() {
        fillAppointmentScrollView();

        //Initialize button click listener for new Appointment
        this.mAppointmentAddNew.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddNewAppointmentActivity.class));
            }
        });
        //Initialize button click listener for new Location, receive a result for a new location
        this.mLocationAddNew.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ChooseLocationActivity.class);
                startActivityForResult(i, REQUEST_NEW_FAVORITE_LOCATION);
            }
        });

        this.mLocationEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditLocationActivity.class));
            }
        });
    }

    private void fillAppointmentScrollView()
    {
        /*
        //Add appointments to the ScrollView
        TextView textViewAppointment = new TextView(MainActivity.this);
        //Clear the text Box first
        textViewAppointment.setText("");
        String testText = "";
        int appointmentListSize = mAppointmentList.size();
        mAppointments = new Appointment[appointmentListSize];
        for (int i = 0; i < appointmentListSize; i++) {
                    Data.Appointment appointment = mAppointmentList.get(i);
                    mAppointments[i] = Appointment.DataHandlerAppointmentToDataAppointment(appointment);
                    testText += mAppointments[i].getmAppointmentText() + "\n";
        }
        textViewAppointment.setText(testText);
        mAppointmentLinearLayout.addView(textViewAppointment);
        */

        if(mAppointmentArrayAdapter == null)
            return;

        for (int i=0; i<mAppointmentArrayAdapter.getCount(); i++)
        {
            View view = mAppointmentArrayAdapter.getView(i, null, null);
            mAppointmentLinearLayout.addView(view);
        }

    }

    private void initData() {
        //Right now, this method only creates a dummy Appointment, later it will read entries from the database
        this.mAppointments = new Appointment[1];
        this.mAppointments[0] = new Appointment(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance().getTime());

        try {
            mAppointmentList = (ArrayList<Data.Appointment>) getDBHelper().getDaoAppointment().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.mAppointmentArrayAdapter = new AppointmentsAdapter(this, mAppointmentList);
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
        this.mLocationAddNew = (Button) findViewById(R.id.button_add_new_location);
        this.mLocationEdit = (Button) findViewById(R.id.buttonEditLocation);

        //Database
        mDatabaseHelper = getDBHelper();
    }

    public DatabaseHelper getDBHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    //Need another method to destroy the database connection as well
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }
    }

    //Just for testing purposes
    public void alertView(Message message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setTitle(message.getData().getString("name"))
                .setMessage(message.getData().getString("text"))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //In general always update the view
        if(resultCode == RESULT_OK)
            fillAppointmentScrollView();

        if(requestCode == REQUEST_NEW_FAVORITE_LOCATION)
        {
            //Do something based on a new location
        }
    }
}
