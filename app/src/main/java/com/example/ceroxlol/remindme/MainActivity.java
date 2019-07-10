package com.example.ceroxlol.remindme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.sql.SQLException;


import DataHandler.AppointmentHandler;
import DatabaseServices.DatabaseHelper;
import GUI.AppointmentsAdapter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAGMainActivity";

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
    private AppointmentHandler[] mAppointmentHandlers;
    private ArrayList<Data.AppointmentHandler> mAppointmentList;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;

    //UI
    private LinearLayout mAppointmentLinearLayout;
    private Button mAppointmentAddNew;
    private Button mLocationAddNew;
    private Button mLocationEdit;
    private ListAdapter mAppointmentListAdapter;
    private RecyclerView mListAppointmentsRecyclerView;

    //Requests
    private final int REQUEST_APP_PERMISSIONS = 1;
    private final int REQUEST_NEW_FAVORITE_LOCATION = 10;
    private final int REQUEST_EDIT_FAVORITE_LOCATION = 11;
    private final int REQUEST_NEW_APPOINTMENT = 20;
    private final int REQUEST_EDIT_APPOINTMENT = 21;

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
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_APP_PERMISSIONS);
        }

    }

    private void initUI() {
        fillAppointmentScrollView();

        this.mAppointmentAddNew.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddNewAppointmentActivity.class);
            startActivityForResult(i, REQUEST_NEW_APPOINTMENT);
        });

        this.mLocationAddNew.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ChooseLocationActivity.class);
            startActivityForResult(i, REQUEST_NEW_FAVORITE_LOCATION);
        });

        this.mLocationEdit.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EditLocationActivity.class);
            startActivityForResult(i, REQUEST_EDIT_FAVORITE_LOCATION);
        });
    }

    private void fillAppointmentScrollView()
    {

        if(mAppointmentListAdapter == null)
            return;

        for (int i = 0; i< mAppointmentListAdapter.getCount(); i++)
        {
            View view = mAppointmentListAdapter.getView(i, null, null);
            mAppointmentLinearLayout.addView(view);
        }

    }

    private void loadAppointmentsFromDatabase{
        try {
            mAppointmentList = (ArrayList<Data.AppointmentHandler>) getDBHelper().getDaoAppointment().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        //Right now, this method only creates a dummy AppointmentHandler, later it will read entries from the database
        this.mAppointmentHandlers = new AppointmentHandler[1];
        this.mAppointmentHandlers[0] = new AppointmentHandler(1, "Test", "This is a test appointment.", mGPSTracker.getLocation(), Calendar.getInstance().getTime());
        this.mAppointmentListAdapter = new AppointmentsAdapter(this, mAppointmentList);
    }

    private void initClasses() {
        //Init GPS Tracker
        this.mGPSTracker = new GPSTracker(this.getApplicationContext());

        //Init checker service thread for appointments met
        this.mAppointmentMetCheckingService = new AppointmentMetCheckingService(this.mGPSTracker, this.mAppointmentHandlers, this);
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
        this.mListAppointmentsRecyclerView = findViewById(R.id.list_appointments_recycler_view);
        this.mListAppointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.setuoListAppointmentsRecyclerView();
        this.mAppointmentAddNew = findViewById(R.id.button_add_new_appointment);
        this.mLocationAddNew = findViewById(R.id.button_add_new_location);
        this.mLocationEdit = findViewById(R.id.button_edit_location);

        //Database
        mDatabaseHelper = getDBHelper();
    }

    private void setuoListAppointmentsRecyclerView() {
        if(mAppointmentListAdapter == null)
            //TODO:
            //implement own ListAdapter class. Use links from Lennart to implement RecycleView
            //mAppointmentListAdapter = new ListAdapter(mAppointmentList);


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
        //super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode: " + requestCode + "  resultCode: " + resultCode);

        if(requestCode == REQUEST_NEW_APPOINTMENT)
        {
            if (resultCode == RESULT_OK)
            {
                Log.d(TAG, "Successfully created new appointment.");
                fillAppointmentScrollView();
            }
        }

        if(requestCode == REQUEST_NEW_FAVORITE_LOCATION)
        {
            //Do something based on a new location
        }
    }

    private void loadScrollView()
    {

    }
}
