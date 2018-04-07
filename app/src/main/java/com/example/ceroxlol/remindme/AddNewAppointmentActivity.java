package com.example.ceroxlol.remindme;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import Data.Appointment;
import Data.FavoriteLocation;
import DatabaseServices.DatabaseHelper;

public class AddNewAppointmentActivity extends AppCompatActivity {

    private DatabaseHelper mDBHelper;

    //UI Elements
    private Button mButtonChooseLocation;
    private Button mButtonSaveAppointment;

    private EditText mEditTextAppointmentNote;
    private EditText mEditTextAppointmentName;
    private TextView mTextViewDate;

    private AutoCompleteTextView mAutoCompleteTextViewNewAppointment;

    private CheckBox mCheckBoxDueDate;


    private Location mChosenLocation;
    private DatePickerDialog mDatePickerDialog;
    private SimpleDateFormat mDateFormatter;

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
        this.mAutoCompleteTextViewNewAppointment = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewNewAppointment);
        this.mCheckBoxDueDate = (CheckBox) findViewById(R.id.checkBoxAddDueDate);
        this.mTextViewDate = (TextView) findViewById(R.id.textViewDate);


        //Location
        List<FavoriteLocation> favoriteLocationsList = mDBHelper.getFavoriteLocationDao().queryForAll();
        String[] favoriteLocationNames = new String[favoriteLocationsList.size()];
        for (int i=0; i < favoriteLocationsList.size(); i++) {
            favoriteLocationNames[i] = favoriteLocationsList.get(i).getName();
        }

        ArrayAdapter<String> favoriteLocations = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, favoriteLocationNames);
        mAutoCompleteTextViewNewAppointment.setAdapter(favoriteLocations);

        //Date
        mDateFormatter = new SimpleDateFormat("dd-mm-yyyy", Locale.GERMANY);
        setDateTimeField();

        //Listener
        this.mButtonSaveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAppointment();
            }
        });

        this.mCheckBoxDueDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    mDatePickerDialog.show();
                }
                else
                    mTextViewDate.setText("");
            }
        });
    }

    private void createNewAppointment()
    {
        Appointment appointment = new Appointment(1, mEditTextAppointmentName.getText().toString(), mEditTextAppointmentNote.getText().toString(), mChosenLocation, mTextViewDate.getText());
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

    private void setDateTimeField() {
        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                mTextViewDate.setText(mDateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }
}
