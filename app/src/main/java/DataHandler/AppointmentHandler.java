package DataHandler;

import android.location.Location;

import java.util.Date;

/**
 * Created by Ceroxlol on 20.04.2017.
 */

public class AppointmentHandler {

    private String mName;
    private String mAppointmentText;
    private Location mLocation;
    private Boolean mHasTime;
    private Date mAppointmentCreated, mAppointmentTime, mAppointmentRemindTime;
    private int mPriority;
    private int mType;
    //private int mPropertiesCount; //Directly deprived from DBHelper and the amount of columns inserted into the appointments table
    //private picture

    //default
    public AppointmentHandler()
    {

    }

    //without time
    public AppointmentHandler(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated)
    {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mHasTime = false;
        //this.mPropertiesCount = 6;
    }

    //with time
    public AppointmentHandler(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated, Date appointmentTime, Date appointmentRemindTime)
    {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = location;
        this.mAppointmentCreated = appointmentCreated;
        this.mAppointmentTime = appointmentTime;
        this.mAppointmentRemindTime = appointmentRemindTime;
        this.mHasTime = true;
        //this.mPropertiesCount = 8;
    }

    //Cast operator
    public static final AppointmentHandler DataHandlerAppointmentToDataAppointment(final Data.AppointmentHandler appointment_to_be_casted){
        AppointmentHandler appointmentHandler = new AppointmentHandler();
        if(appointment_to_be_casted.getmHasTime()) {
             appointmentHandler = new AppointmentHandler(appointment_to_be_casted.getmType(), appointment_to_be_casted.getmName(),
                    appointment_to_be_casted.getmAppointmentText(), appointment_to_be_casted.getmLocation(), appointment_to_be_casted.getmAppointmentCreated(),
                    appointment_to_be_casted.getmAppointmentTime(), appointment_to_be_casted.getmAppointmentRemindTime());
        }
        else if(!appointment_to_be_casted.getmHasTime()){
            appointmentHandler = new AppointmentHandler(appointment_to_be_casted.getmType(), appointment_to_be_casted.getmName(),
                    appointment_to_be_casted.getmAppointmentText(), appointment_to_be_casted.getmLocation(), appointment_to_be_casted.getmAppointmentCreated());
            }
            return appointmentHandler;
    }

    //Public methods

    public boolean checkIfAppointmentDistanceIsMet(Location currentLocation) {
        if(this.mLocation.distanceTo(currentLocation) < 50)
            return true;
        return false;
    }

    //TODO: Ändere getter und setter indem das "m" gestrichen wird

    //GETTER
    public String getmAppointmentText() {
        return mAppointmentText;
    }

    public Location getmLocation() {
        return mLocation;
    }

    public Boolean getmHasTime() {
        return mHasTime;
    }

    public Date getmAppointmentCreated() {
        return mAppointmentCreated;
    }

    public Date getmAppointmentTime() {
        return mAppointmentTime;
    }

    public Date getmAppointmentRemindTime() {
        return mAppointmentRemindTime;
    }

    public int getmPriority() {
        return mPriority;
    }

    public int getmType() {
        return mType;
    }

    public String getmName() {return mName;}

    //SETTER
    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmAppointmentText(String mAppointmentText) {
        this.mAppointmentText = mAppointmentText;
    }

    public void setmLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public void setmHasTime(Boolean mHasTime) {
        this.mHasTime = mHasTime;
    }

    public void setmAppointmentCreated(Date mAppointmentCreated) {
        this.mAppointmentCreated = mAppointmentCreated;
    }

    public void setmAppointmentTime(Date mAppointmentTime) {
        this.mAppointmentTime = mAppointmentTime;
    }

    public void setmAppointmentRemindTime(Date mAppointmentRemindTime) {
        this.mAppointmentRemindTime = mAppointmentRemindTime;
    }

    public void setmPriority(int mPriority) {
        this.mPriority = mPriority;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }
}
