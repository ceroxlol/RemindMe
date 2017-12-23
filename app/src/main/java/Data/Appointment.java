package Data;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import DatabaseServices.LocationPersister;

/**
 * Created by Ceroxlol on 13.12.2017.
 */
@DatabaseTable
public class Appointment extends DataHandler.Appointment{

    @DatabaseField(generatedId = true, columnName = "AppointmentID")
    private int id;
    @DatabaseField
    private String mName;
    @DatabaseField
    private String mAppointmentText;
    @DatabaseField(persisterClass = LocationPersister.class)
    private Location mLocation;
    @DatabaseField
    private Boolean mHasTime;
    @DatabaseField
    private Date mAppointmentCreated, mAppointmentTime, mAppointmentRemindTime;
    @DatabaseField
    private int mPriority;
    @DatabaseField
    private int mType;

    private enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time};

    public Appointment()
    {
        // ORMLite needs a no-arg constructor
    }

    //without time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated)
    {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mHasTime = false;
    }

    //with time
    public Appointment(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated, Date appointmentTime, Date appointmentRemindTime)
    {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mAppointmentTime = appointmentTime;
        this.mAppointmentRemindTime = appointmentRemindTime;
        this.mHasTime = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmAppointmentText() {
        return mAppointmentText;
    }

    public void setmAppointmentText(String mAppointmentText) {
        this.mAppointmentText = mAppointmentText;
    }

    public Location getmLocation() {
        return mLocation;
    }

    public void setmLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public Boolean getmHasTime() {
        return mHasTime;
    }

    public void setmHasTime(Boolean mHasTime) {
        this.mHasTime = mHasTime;
    }

    public Date getmAppointmentCreated() {
        return mAppointmentCreated;
    }

    public void setmAppointmentCreated(Date mAppointmentCreated) {
        this.mAppointmentCreated = mAppointmentCreated;
    }

    public Date getmAppointmentTime() {
        return mAppointmentTime;
    }

    public void setmAppointmentTime(Date mAppointmentTime) {
        this.mAppointmentTime = mAppointmentTime;
    }

    public Date getmAppointmentRemindTime() {
        return mAppointmentRemindTime;
    }

    public void setmAppointmentRemindTime(Date mAppointmentRemindTime) {
        this.mAppointmentRemindTime = mAppointmentRemindTime;
    }

    public int getmPriority() {
        return mPriority;
    }

    public void setmPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }
}
