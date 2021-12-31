package com.example.ceroxlol.remindme.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ceroxlol.remindme.R;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.example.ceroxlol.remindme.models.Appointment;
import com.example.ceroxlol.remindme.models.FavoriteLocation;
import com.example.ceroxlol.remindme.fragments.DatePickerFragment;
import com.example.ceroxlol.remindme.adapters.ArrayAdapterLocationsListSpinner;

public class AddNewAppointmentActivity extends AppCompatActivity {

    private Button buttonAppointmentDate;
    private EditText editTextAppointmentText;
    private EditText editTextAppointmentName;
    private Spinner spinnerAddAppointmentLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_appointment);

        init();
    }

    private void init() {

        //TODO: Add maps fragment with the corresponding location so you can draw a circle around it
        editTextAppointmentName = findViewById(R.id.editTextAddAppointmentAppointmentName);
        editTextAppointmentText = findViewById(R.id.editTextAddAppointmentAppointmentText);
        buttonAppointmentDate = findViewById(R.id.buttonAddAppointmentDate);
        spinnerAddAppointmentLocations = findViewById(R.id.spinnerAddAppointmentLocations);

        Button mButtonAppointmentSave = findViewById(R.id.buttonAddAppointmentSave);

        this.buttonAppointmentDate.setOnClickListener(view -> {
            DatePickerFragment datePickerDialog = new DatePickerFragment(R.id.buttonAddAppointmentDate);
            datePickerDialog.show(getFragmentManager(), "Date Picker");
        });

        mButtonAppointmentSave.setOnClickListener(v -> {
            Appointment appointment = saveNewAppointment();
            finishActivity(appointment);
        });

        loadLocations();
    }

    private void loadLocations() {
        ArrayList<FavoriteLocation> locations = (ArrayList<FavoriteLocation>) MainActivity.databaseHelper.getFavoriteLocationDaoRuntimeException().queryForAll();
        ArrayAdapterLocationsListSpinner adapter = new ArrayAdapterLocationsListSpinner(this, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        this.spinnerAddAppointmentLocations.setAdapter(adapter);
    }

    private Appointment saveNewAppointment() {
        FavoriteLocation favoriteLocation = (FavoriteLocation) this.spinnerAddAppointmentLocations.getSelectedItem();
        Date appointmentTime = null;
        Appointment appointment;

        String date = this.buttonAppointmentDate.getText().toString();
        if(!date.equals("No Date")){
            try {
                appointmentTime = new SimpleDateFormat("dd MM yyyy HH:mm", Locale.GERMAN).parse(this.buttonAppointmentDate.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (appointmentTime != null) {
            appointment = new Appointment(1, editTextAppointmentName.getText().toString(),
                    editTextAppointmentText.getText().toString(), favoriteLocation, Calendar.getInstance().getTime(),
                    appointmentTime);
        } else {
            appointment = new Appointment(1, editTextAppointmentName.getText().toString(),
                    editTextAppointmentText.getText().toString(), favoriteLocation, Calendar.getInstance().getTime());
        }

        try {
            MainActivity.databaseHelper.getAppointmentDao().create(appointment);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointment;
    }

    //When finishing this activity, an acknowledge to the main activity is sent to refresh the appointment list
    private void finishActivity(Appointment appointment) {
        Intent i = new Intent();
        i.putExtra("appointment", appointment);
        setResult(RESULT_OK, i);
        finish();
    }
}
