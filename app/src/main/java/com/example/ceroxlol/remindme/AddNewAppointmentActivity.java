package com.example.ceroxlol.remindme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import Data.Appointment;
import Data.FavoriteLocation;
import DatabaseServices.DatabaseHelper;
import Fragments.DatePickerFragment;
import adapter.ArrayAdapterLocationsListSpinner;

public class AddNewAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;

    private Button mButtonAppointmentDate;
    private Button mButtonAppointmentSave;
    private EditText mEditTextAppointmentText;
    private EditText mEditTextAppointmentName;
    private Spinner mSpinnerAddAppointmentLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_appointment);

        init();
    }

    private void init() {
        this.mDBHelper = MainActivity.mDatabaseHelper;

        this.mEditTextAppointmentName = findViewById(R.id.editTextAddAppointmentAppointmentName);
        this.mEditTextAppointmentText = findViewById(R.id.editTextAddAppointmentAppointmentText);
        this.mButtonAppointmentDate = findViewById(R.id.buttonAddAppointmentDate);
        this.mButtonAppointmentSave = findViewById(R.id.buttonAddAppointmentSave);
        this.mSpinnerAddAppointmentLocations = findViewById(R.id.spinnerAddAppointmentLocations);

        this.mButtonAppointmentDate.setOnClickListener(view -> {
            DatePickerFragment datePickerDialog = new DatePickerFragment(R.id.buttonAddAppointmentDate);
            datePickerDialog.show(getFragmentManager(), "Date Picker");
        });

        this.mButtonAppointmentSave.setOnClickListener(v -> {
            saveNewAppointment();
            finishActivity();
        });

        loadLocations();
    }

    private void loadLocations() {
        ArrayList<FavoriteLocation> locations = (ArrayList<FavoriteLocation>) mDBHelper.getFavoriteLocationDaoRuntimeException().queryForAll();
        ArrayAdapterLocationsListSpinner adapter = new ArrayAdapterLocationsListSpinner(this, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        this.mSpinnerAddAppointmentLocations.setAdapter(adapter);
    }

    private void saveNewAppointment() {
        FavoriteLocation favoriteLocation = (FavoriteLocation) this.mSpinnerAddAppointmentLocations.getSelectedItem();
        Date appointmentTime = null;
        Appointment appointment;

        try {
            appointmentTime = new SimpleDateFormat("dd MM yyyy HH:mm", Locale.GERMAN).parse(this.mButtonAppointmentDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (appointmentTime != null)
            appointment = new Appointment(1, mEditTextAppointmentName.getText().toString(),
                mEditTextAppointmentText.getText().toString(), favoriteLocation, Calendar.getInstance().getTime(),
                appointmentTime);
        //No appointment Time
        else
            appointment = new Appointment(1, mEditTextAppointmentName.getText().toString(),
                    mEditTextAppointmentText.getText().toString(), favoriteLocation, Calendar.getInstance().getTime());

        try {
            mDBHelper.getAppointmentDao().create(appointment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //When finishing this acitivity, an acknowledge to the main acitivity is sent to refresh the appointment list
    private void finishActivity() {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        finish();
    }
}
