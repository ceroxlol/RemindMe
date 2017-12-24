package com.example.ceroxlol.remindme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.sql.SQLException;
import java.util.Calendar;

import Data.Appointment;
import DatabaseServices.DatabaseHelper;

public class AddNewAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;

    //UI Elements
    private Button mButtonChooseLocation;
    private Button mButtonSaveAppointment;

    private EditText mEditTextAppointmentNote;
    private EditText mEditTextAppointmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_appointment);

        init();
    }

    private void init() {
        this.mDBHelper = MainActivity.mDatabaseHelper;

        //UI
        this.mButtonChooseLocation = (Button) findViewById(R.id.buttonChooseLocation);
        this.mButtonSaveAppointment = (Button) findViewById(R.id.buttonSaveAppointment);
        this.mEditTextAppointmentNote = (EditText) findViewById(R.id.editTextAppointmentNote);
        this.mEditTextAppointmentName = (EditText) findViewById(R.id.editTextAppointmentName);

        this.mButtonSaveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAppointment();
            }
        });
    }

    private void createNewAppointment()
    {
        Appointment appointment = new Appointment(1, mEditTextAppointmentName.getText().toString(), mEditTextAppointmentNote.getText().toString(), null, Calendar.getInstance().getTime());
        try {
            mDBHelper.getDaoAppointment().create(appointment);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finishActivity();
    }

    //When finishing this acitivity, an acknowledge to the main acitivity is sent to refresh the appointment list
    private void finishActivity()
    {
        Intent i = new Intent();
        setResult(RESULT_OK,i);
        finish();
    }
}
