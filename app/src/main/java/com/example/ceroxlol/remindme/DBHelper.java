package com.example.ceroxlol.remindme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static android.content.ContentValues.TAG;


/**
 * Created by Ceroxlol on 01.05.2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    //TEST
    private boolean test = true;


    private static final String DATABASE_NAME = "appointmentsDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_APPOINTMENTS = "appointments";
    private static final String TABLE_USERS = "users";

    // Appointments Table Columns
    // appointments
    private static final String KEY_APPOINTMENT_ID = "id";
    private static final String KEY_APPOINTMENT = "appointment";
    private static final String KEY_USER = "user";
    private static final String KEY_IS_ACTIVE = "is_active";

    // users
    private static final String KEY_USERS_ID = "id";
    private static final String KEY_USERS_USER_NAME = "user_anme";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Created Database Appointments");
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APPOINTMENTS_TABLE = "CREATE TABLE " + TABLE_APPOINTMENTS +
                "(" +
                KEY_APPOINTMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + // Define a primary key
                KEY_APPOINTMENT + " TEXT," +
                KEY_USER + " TEXT," +
                KEY_IS_ACTIVE + " TEXT" +
                ")";

        db.execSQL(CREATE_APPOINTMENTS_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS +
                "(" +
                KEY_USERS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + // Define a primary key
                KEY_USERS_USER_NAME + " TEXT" +
                ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
            onCreate(db);
        }
    }

    //table might be left out, then the implicit choice of appointments table is taken
    //columns might be emtpy due to only being used in the select and insert statement
    private void executeInsertUpdate(String statement, String[] columns, String[] tables, String[] values, String[] conditions)
    {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        //Statement that will be executed onto the database
        String execute_statement = "";

        if(tables[0] == "")
            tables[0] = TABLE_APPOINTMENTS;

        //Try to insert
        try {
            switch (statement) {
                case "Insert":
                    execute_statement = "INSERT INTO " + tables[0] + " VALUES ";
                    for (String val : values
                            ) {
                        execute_statement += "('" + val + "', ";
                    }
                    execute_statement = execute_statement.substring(0, execute_statement.length() - 2);
                    execute_statement += ");";
                    break;
                case "Update":
                    break;
                case "Delete":
                    break;
                default:
                    return;
            }
            db.execSQL(execute_statement);
        }
        catch (Exception e) {
            Log.d(TAG, "Error while trying to add appointment to database.\n" + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    public Appointment[] executeSelectAppointments(String statement, String[] conditions)
    {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        //Statement that will be executed onto the database
        String execute_statement = "";

        List<Appointment> appointments = new ArrayList<Appointment>();

        //Try to select
        try {
            switch (statement.toUpperCase()) {
                case "SELECTWHERE":
                    break;
                case "SELECT*":
                    execute_statement = "SELECT * FROM " + TABLE_APPOINTMENTS + ";";
                    break;
                case "SELECT*WHERE":
                    execute_statement = "SELECT * FROM " + TABLE_APPOINTMENTS + "WHERE " + conditions[0] + ";";
                    break;
                default:
                    return (Appointment[]) appointments.toArray();
            }

            Cursor cursor = db.rawQuery(execute_statement, null);

            //move to first entry
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                appointments.add(insertCursorEntryIntoAppointment(cursor));
                cursor.moveToNext();
            }
        }
        catch (Exception e) {
            Log.d(TAG, "Error while trying to select appointment form database");
        } finally {
        db.endTransaction();
        }

        return convertArrayListToAppointmentArray(appointments);
    }

    //Helper function for casting the array list to an array
    private Appointment[] convertArrayListToAppointmentArray(List<Appointment> appointments_array_list)
    {
        Appointment[] appointments = new Appointment[appointments_array_list.size()];

        for (int i = 0; i < appointments.length; i++) {
            appointments[i] = (Appointment) appointments_array_list.get(i);
        }
        return appointments;
    }

    private Appointment insertCursorEntryIntoAppointment(Cursor cursor)
    {
        Gson gson = new Gson();
        Appointment returnAppointment = gson.fromJson(cursor.getString(2), Appointment.class);

        return returnAppointment;
    }


    public void insertNewAppointment(Appointment appointment) {
        Gson gson = new GsonBuilder().create();
        //Gather Database objects
        String toStoreObject = gson.toJson(appointment, Appointment.class);
        String user = getUser();

        String[] table = {TABLE_APPOINTMENTS};
        String[] values = {toStoreObject};
        executeInsertUpdate("Insert", new String[0], table, values, new String[0]);
    }

    //Call Select Statement for appointments assoiciated with this account and return the list sorted by ascending date
    public Appointment[] getAppointments(String user)
    {
        return executeSelectAppointments("SELECT*", new String[0]);
    }

    public String getUser() {
        if(test)
            return "Test";
        else
            //return exe;
        return "fix";
    }

    /*private executeSelectUser(String statement, String[] conditions)
    {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        try {

        }catch(Exception e)
        {
            Log.d(TAG, "Error while trying to select users form database");
        } finally {
            db.endTransaction();
        }
    }*/


    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
