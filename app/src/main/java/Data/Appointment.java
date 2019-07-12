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
public class Appointment extends DataHandler.AppointmentHandler {

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

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getAppointmentText() {
        return mAppointmentText;
    }

    public void setAppointmentText(String mAppointmentText) {
        this.mAppointmentText = mAppointmentText;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public Boolean getHasTime() {
        return mHasTime;
    }

    public void setHasTime(Boolean mHasTime) {
        this.mHasTime = mHasTime;
    }

    public Date getAppointmentCreated() {
        return mAppointmentCreated;
    }

    public void setAppointmentCreated(Date mAppointmentCreated) {
        this.mAppointmentCreated = mAppointmentCreated;
    }

    public Date getAppointmentTime() {
        return mAppointmentTime;
    }

    public void setAppointmentTime(Date mAppointmentTime) {
        this.mAppointmentTime = mAppointmentTime;
    }

    public Date getAppointmentRemindTime() {
        return mAppointmentRemindTime;
    }

    public void setAppointmentRemindTime(Date mAppointmentRemindTime) {
        this.mAppointmentRemindTime = mAppointmentRemindTime;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }
}
