package DataHandler;

import android.location.Location;

import java.util.Date;

import Data.Appointment;

/**
 * Created by Ceroxlol on 20.04.2017.
 */

public class AppointmentHandler {

    public Appointment mAppointment;

    //private int mPropertiesCount; //Directly deprived from DBHelper and the amount of columns inserted into the appointments table
    //private picture

    //default
    public AppointmentHandler()
    {

    }

    //without time
    public AppointmentHandler(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated)
    {
        this.mAppointment = new Appointment();
        this.mAppointment.setType(appointmentType);
        this.mAppointment.setName(name);
        this.mAppointment.setAppointmentText(appointmentText);
        this.mAppointment.setLocation(location);
        this.mAppointment.setAppointmentCreated(appointmentCreated);
        this.mAppointment.setHasTime(false);
    }

    //with time
    public AppointmentHandler(int appointmentType, String name, String appointmentText, Location location, Date appointmentCreated, Date appointmentTime, Date appointmentRemindTime)
    {
        this.mAppointment = new Appointment();
        this.mAppointment.setType(appointmentType);
        this.mAppointment.setName(name);
        this.mAppointment.setAppointmentText(appointmentText);
        this.mAppointment.setLocation(location);
        this.mAppointment.setAppointmentCreated(appointmentCreated);
        this.mAppointment.setAppointmentTime(appointmentTime);
        this.mAppointment.setAppointmentRemindTime(appointmentRemindTime);
        this.mAppointment.setHasTime(true);
    }

    /*
    //Cast operator
    public static final AppointmentHandler DataHandlerAppointmentToDataAppointment(final Appointment appointment_to_be_casted){
        AppointmentHandler appointmentHandler = new AppointmentHandler();
        if(appointment_to_be_casted.getmHasTime()) {
             appointmentHandler = new AppointmentHandler(appointment_to_be_casted.getmType(), appointment_to_be_casted.getmName(),
                    appointment_to_be_casted.getAppointmentText(), appointment_to_be_casted.getmLocation(), appointment_to_be_casted.getmAppointmentCreated(),
                    appointment_to_be_casted.getmAppointmentTime(), appointment_to_be_casted.getmAppointmentRemindTime());
        }
        else if(!appointment_to_be_casted.getmHasTime()){
            appointmentHandler = new AppointmentHandler(appointment_to_be_casted.getmType(), appointment_to_be_casted.getmName(),
                    appointment_to_be_casted.getAppointmentText(), appointment_to_be_casted.getmLocation(), appointment_to_be_casted.getmAppointmentCreated());
            }
            return appointmentHandler;
    }
    */

    public boolean checkIfAppointmentDistanceIsMet(Location currentLocation) {
        if(this.mAppointment.getLocation().distanceTo(currentLocation) < 50)
            return true;
        return false;
    }
}
