package com.example.ceroxlol.remindme.models;

import android.location.Location;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import com.example.ceroxlol.remindme.utils.LocationPersister;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Ceroxlol on 13.12.2017.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable
public class Appointment {

    @DatabaseField(generatedId = true, columnName = "AppointmentID")
    private int id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String appointmentText;
    @DatabaseField(persisterClass = LocationPersister.class)
    private Location location;
    @DatabaseField(foreign = true)
    private FavoriteLocation favoriteLocation;
    @DatabaseField
    private Boolean hasTime;
    //DATE FORMAT: dd/MM/yyyy HH mm
    @DatabaseField(dataType = DataType.DATE_STRING, format = "dd MM yyyy HH:mm")
    private Date appointmentCreated;
    @DatabaseField(dataType = DataType.DATE_STRING, format = "dd MM yyyy HH:mm")
    private Date appointmentTime;
    @DatabaseField
    private int priority;
    @DatabaseField
    private int type;
    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean isActive;

    //private enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time}

    //without time
    public Appointment(int appointmentType, String name, String appointmentText, FavoriteLocation favoriteLocation, Date appointmentCreated) {
        this.type = appointmentType;
        this.name = name;
        this.appointmentText = appointmentText;
        this.location = favoriteLocation.getLocation();
        this.favoriteLocation = favoriteLocation;
        this.appointmentCreated = appointmentCreated;
        this.hasTime = false;
    }

    //with time
    public Appointment(int appointmentType, String name, String appointmentText, FavoriteLocation favoriteLocation, Date appointmentCreated, Date appointmentTime) {
        this.type = appointmentType;
        this.name = name;
        this.appointmentText = appointmentText;
        this.location = favoriteLocation.getLocation();
        this.favoriteLocation = favoriteLocation;
        this.appointmentCreated = appointmentCreated;
        this.appointmentTime = appointmentTime;
        this.hasTime = true;
    }
}

