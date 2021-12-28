package Data;

import android.location.Location;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import DatabaseServices.LocationPersister;
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
    private String mName;
    @DatabaseField
    private String mAppointmentText;
    @DatabaseField(persisterClass = LocationPersister.class)
    private Location mLocation;
    @DatabaseField(foreign = true)
    private FavoriteLocation mFavoriteLocation;
    @DatabaseField
    private Boolean mHasTime;
    //DATE FORMAT: dd/MM/yyyy HH mm
    @DatabaseField(dataType = DataType.DATE_STRING, format = "dd MM yyyy HH:mm")
    private Date mAppointmentCreated;
    @DatabaseField(dataType = DataType.DATE_STRING, format = "dd MM yyyy HH:mm")
    private Date mAppointmentTime;
    @DatabaseField
    private int mPriority;
    @DatabaseField
    private int mType;
    @DatabaseField(canBeNull = false, defaultValue = "true")
    private Boolean mIsActive;

    //private enum mAppointmentType {Arrival, Leave, ArrivalWithTime, LeaveWithTime, Time}

    //without time
    public Appointment(int appointmentType, String name, String appointmentText, FavoriteLocation favoriteLocation, Date appointmentCreated) {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = favoriteLocation.getLocation();
        this.mFavoriteLocation = favoriteLocation;
        this.mAppointmentCreated = appointmentCreated;
        this.mHasTime = false;
    }

    //with time
    public Appointment(int appointmentType, String name, String appointmentText, FavoriteLocation favoriteLocation, Date appointmentCreated, Date appointmentTime) {
        this.mType = appointmentType;
        this.mName = name;
        this.mAppointmentText = appointmentText;
        this.mLocation = favoriteLocation.getLocation();
        this.mFavoriteLocation = favoriteLocation;
        this.mAppointmentCreated = appointmentCreated;
        this.mAppointmentTime = appointmentTime;
        this.mHasTime = true;
    }
}

