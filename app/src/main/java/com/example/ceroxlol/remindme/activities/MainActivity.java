package com.example.ceroxlol.remindme.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.ceroxlol.remindme.fragments.AppointmentsFragment;
import com.example.ceroxlol.remindme.models.AppointmentKT;
import com.example.ceroxlol.remindme.utils.AppointmentMetCheckingService;
import com.example.ceroxlol.remindme.utils.GpsTracker;
import com.example.ceroxlol.remindme.R;
import com.google.android.material.tabs.TabLayout;

import com.example.ceroxlol.remindme.adapters.MainPageAdapter;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final boolean APPOINTMENT_TRACKER_ENABLED = false;

    private GpsTracker gpsTracker;

    private AppointmentMetCheckingService appointmentMetCheckingService;

    private Button buttonAddNewAppointment;
    private Button buttonEditAppointment;
    private Button addNewLocation;
    private Button editLocation;

    private final int REQUEST_APP_PERMISSIONS = 1;
    private final int REQUEST_NEW_FAVORITE_LOCATION = 10;
    private final int REQUEST_EDIT_FAVORITE_LOCATION = 11;
    private final int REQUEST_CREATE_NEW_APPOINTMENT = 20;
    private final int REQUEST_EDIT_APPOINTMENT = 21;

    private MainPageAdapter mainPageAdapter;

    private ActivityResultLauncher<Intent> addAppointmentActivityResultLauncher;

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Implement good layout first, then: Filter appointments

        init();
    }

    private void init() {
        checkPermissions();
        initClasses();
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
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        gpsTracker = new GpsTracker(getApplicationContext());

        if (APPOINTMENT_TRACKER_ENABLED) {
            appointmentMetCheckingService = new AppointmentMetCheckingService(gpsTracker, this);
            appointmentMetCheckingService.setRun(true);
            appointmentMetCheckingService.start();
        }

        //TODO: Improve the layout for appointments to be shown
        //Make them expandable. Show only name and time?
        buttonAddNewAppointment = findViewById(R.id.buttonAddNewAppointment);
        buttonEditAppointment = findViewById(R.id.buttonEditAppointment);
        addNewLocation = findViewById(R.id.buttonAddNewLocation);
        editLocation = findViewById(R.id.buttonEditLocation);

        ViewPager viewPager = findViewById(R.id.viewPager);
        mainPageAdapter = new MainPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainPageAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        addAppointmentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::updateRecyclerview);
    }

    private void updateRecyclerview(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            AppointmentKT appointment = Objects.requireNonNull(result.getData()).getParcelableExtra("appointment");
            AppointmentsFragment appointmentsFragment = (AppointmentsFragment) mainPageAdapter.getItem(0);
            if (appointment != null) {
                appointmentsFragment.insertData(appointment);
            }
        }
    }

    private void initUI() {
        this.buttonAddNewAppointment.setOnClickListener(v -> {
            if (!checkIfLocationsAreAvailable()) {
                showNoLocationsAvailableAlertDialog();
            } else {
                Intent i = new Intent(MainActivity.this, AddNewAppointmentActivity.class);
                addAppointmentActivityResultLauncher.launch(i);
            }
        });

        this.buttonEditAppointment.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EditAppointmentsFragment.class);
            startActivityForResult(i, REQUEST_EDIT_APPOINTMENT);
        });

        this.addNewLocation.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ChooseLocationActivity.class);
            startActivityForResult(i, REQUEST_NEW_FAVORITE_LOCATION);
        });

        this.editLocation.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EditLocationActivity.class);
            startActivityForResult(i, REQUEST_EDIT_FAVORITE_LOCATION);
        });
    }

    private void showNoLocationsAvailableAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("No locations available");

        // Setting Dialog Message
        alertDialog.setMessage("In order to create appointments you need to have locations first. Please create one.");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing OK button
        alertDialog.setPositiveButton("OK", null);

        // Showing Alert Message
        alertDialog.show();
    }

    private boolean checkIfLocationsAreAvailable() {
        return getDb().locationMarkerDao().entriesExist();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "requestCode: " + requestCode + "  resultCode: " + resultCode);

        if (requestCode == REQUEST_CREATE_NEW_APPOINTMENT) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Successfully created new appointment.");

            }
        }

        if (requestCode == REQUEST_NEW_FAVORITE_LOCATION) {
            //Do something based on a new location
        }
    }
}
