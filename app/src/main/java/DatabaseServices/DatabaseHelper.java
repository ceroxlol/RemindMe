package DatabaseServices;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import Data.Appointment;
import Data.User;

/**
 * Created by Ceroxlol on 18.12.2017.
 */

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    //Database variables
    private static final String DATABASE_NAME = "RemindMe.db";
    private static final int DATABASE_VERSION = 1;
    public static Gson gson;

    //DAO Objects to access the tables
    private Dao<Appointment, Integer> appointmentDao = null;
    private RuntimeExceptionDao<Appointment, Integer> appointmentRuntimeDao = null;

    private Dao<User, Integer> userDao = null;
    private RuntimeExceptionDao<User, Integer> userRuntimeDao = null;

    public DatabaseHelper(Context context){
        //TODO: Look if the app is performing good although no config is used
        //super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        gson = new GsonBuilder().create();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Appointment.class);
            TableUtils.createTable(connectionSource, User.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        // here we try inserting data in the on-create as a test
        RuntimeExceptionDao<Appointment, Integer> dao = getDaoAppointmentRuntimeException();
        // create some entries in the onCreate
        Appointment appointment = new Appointment(1, "Test", "Test", null, Calendar.getInstance().getTime());
        dao.create(appointment);
        appointment = new Appointment(2, "Test2", "Test2", null, Calendar.getInstance().getTime());
        dao.create(appointment);
        Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Appointment.class, true);
            TableUtils.dropTable(connectionSource, User.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Appointment, Integer> getDaoAppointment() throws SQLException {
        if (appointmentDao == null) {
            appointmentDao = getDao(Appointment.class);
        }
        return appointmentDao;
    }

    public RuntimeExceptionDao<Appointment,Integer> getDaoAppointmentRuntimeException() {
        if(appointmentRuntimeDao == null)
            appointmentRuntimeDao = getRuntimeExceptionDao(Appointment.class);
        return appointmentRuntimeDao;
    }


    public Dao<User, Integer> getDaoUser() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }

    public RuntimeExceptionDao<User,Integer> getUserDao() {
        if(userRuntimeDao == null)
            userRuntimeDao = getRuntimeExceptionDao(User.class);
        return userRuntimeDao;
    }

    @Override
    public void close() {
        super.close();
        appointmentDao = null;
        appointmentRuntimeDao = null;
    }





    //AndroidDatabaseManager
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
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
