package com.example.ceroxlol.remindme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;


import Data.Appointment;
import DatabaseServices.DatabaseHelper;
import adapter.RecyclerViewListAdapterAppointments;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //PUBLIC
    //Enum for Type
    public static enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time}
    //Database
    public static DatabaseHelper mDatabaseHelper = null;

    //PRIVATE
    //GPS Component
    private GPSTracker mGPSTracker;

    //Appointments
    private ArrayList<Appointment> mAppointmentArrayList;
    private AppointmentMetCheckingService mAppointmentMetCheckingService;

    //UI
    private LinearLayout mAppointmentLinearLayout;
    private Button mButtonAddNewAppointment;
    private Button mButtonEditAppointment;
    private Button mAddNewLocation;
    private Button mEditLocation;
    private RecyclerViewListAdapterAppointments mRecyclerViewListAdapterAppointments;
    private RecyclerView mRecyclerViewAppointmentList;

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

        //TODO: Filter appointments
        //TODO: Setup navigation bar for every activity

        //Call program setup
        init();
    }

    private void init() {

        //Permission Check
        checkPermissions();

        //Init components
        initClasses();

        //Init UI
        initUI();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_APP_PERMISSIONS);
        }
    }

    private void initClasses() {
        //Init GPS Tracker
        this.mGPSTracker = new GPSTracker(this.getApplicationContext());

        //Database
        getDBHelper();

        this.mAppointmentArrayList = (ArrayList<Appointment>) mDatabaseHelper.getAppointmentDaoRuntimeException().queryForAll();

        //Init checker service thread for appointments met
        this.mAppointmentMetCheckingService = new AppointmentMetCheckingService(this.mGPSTracker, this);
        this.mAppointmentMetCheckingService.setRun(true);
        this.mAppointmentMetCheckingService.start();

        //TODO: Improve the layout for appointments to be shown
        //Make them expandable. Show only name and time?
        //UI elements
        this.mRecyclerViewAppointmentList = findViewById(R.id.RecyclerViewAppointmentList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        this.mRecyclerViewAppointmentList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.mRecyclerViewAppointmentList.getContext(),
                ((LinearLayoutManager) layoutManager).getOrientation());
        this.mRecyclerViewAppointmentList.addItemDecoration(dividerItemDecoration);
        this.mButtonAddNewAppointment = findViewById(R.id.buttonAddNewAppointment);
        this.mButtonEditAppointment = findViewById(R.id.buttonEditAppointment);
        this.mAddNewLocation = findViewById(R.id.buttonAddNewLocation);
        this.mEditLocation = findViewById(R.id.buttonEditLocation);

        if (mRecyclerViewListAdapterAppointments == null) {
            mRecyclerViewListAdapterAppointments = new RecyclerViewListAdapterAppointments(this.mAppointmentArrayList);
            mRecyclerViewAppointmentList.setAdapter(mRecyclerViewListAdapterAppointments);
        }
    }

    private void initUI() {
        this.mButtonAddNewAppointment.setOnClickListener(v -> {
            if (!checkIfLocationsAreAvailable()) {
                showAlertDialog("No locations available",
                        "In order to create appointments you need to have locations first. Please create one.");
            } else {
                Intent i = new Intent(MainActivity.this, AddNewAppointmentActivity.class);
                startActivityForResult(i, REQUEST_NEW_APPOINTMENT);
            }
        });

        this.mButtonEditAppointment.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EditAppointmentActivity.class);
            startActivityForResult(i, REQUEST_EDIT_APPOINTMENT);
        });

        this.mAddNewLocation.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ChooseLocationActivity.class);
            startActivityForResult(i, REQUEST_NEW_FAVORITE_LOCATION);
        });

        this.mEditLocation.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EditLocationActivity.class);
            startActivityForResult(i, REQUEST_EDIT_FAVORITE_LOCATION);
        });
    }

    private void showAlertDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing OK button
        alertDialog.setPositiveButton("OK", null);

        // Showing Alert Message
        alertDialog.show();
    }

    private boolean checkIfLocationsAreAvailable() {
        return this.getDBHelper().getFavoriteLocationDaoRuntimeException().queryForAll().size() != 0;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode: " + requestCode + "  resultCode: " + resultCode);

        if (requestCode == REQUEST_NEW_APPOINTMENT) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Successfully created new appointment.");
            }
        }

        if (requestCode == REQUEST_NEW_FAVORITE_LOCATION) {
            //Do something based on a new location
        }

        refreshAppointmentList();
    }

    private void refreshAppointmentList() {
        this.mAppointmentArrayList = (ArrayList<Appointment>) mDatabaseHelper.getAppointmentDaoRuntimeException().queryForAll();
        this.mRecyclerViewListAdapterAppointments.setAppointmentList(mAppointmentArrayList);
        this.mRecyclerViewListAdapterAppointments.notifyDataSetChanged();
    }
}
