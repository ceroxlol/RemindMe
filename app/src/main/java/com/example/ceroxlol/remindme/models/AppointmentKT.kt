package com.example.ceroxlol.remindme.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.Builder
import java.util.*

@Entity
data class AppointmentKT(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo var name: String,
    @ColumnInfo var text: String?,
    @ColumnInfo var location: LocationMarker,
    @ColumnInfo val created: Date = Date(System.currentTimeMillis()),
    @ColumnInfo var time: Date?,
    @ColumnInfo var done: Boolean
)

/*
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
 */