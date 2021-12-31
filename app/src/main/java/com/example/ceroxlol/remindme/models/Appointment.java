package com.example.ceroxlol.remindme.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

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
public class Appointment implements Parcelable {

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

    protected Appointment(Parcel in) {
        id = in.readInt();
        name = in.readString();
        appointmentText = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        byte tmpHasTime = in.readByte();
        hasTime = tmpHasTime == 0 ? null : tmpHasTime == 1;
        priority = in.readInt();
        type = in.readInt();
        byte tmpIsActive = in.readByte();
        isActive = tmpIsActive == 0 ? null : tmpIsActive == 1;
    }

    public static final Creator<Appointment> CREATOR = new Creator<Appointment>() {
        @Override
        public Appointment createFromParcel(Parcel in) {
            return new Appointment(in);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };

    //TODO: Implement Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(appointmentText);
        dest.writeParcelable(location, flags);
        dest.writeByte((byte) (hasTime == null ? 0 : hasTime ? 1 : 2));
        dest.writeInt(priority);
        dest.writeInt(type);
        dest.writeByte((byte) (isActive == null ? 0 : isActive ? 1 : 2));
    }
}

