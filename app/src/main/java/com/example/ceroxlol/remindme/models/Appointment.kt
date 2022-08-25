package com.example.ceroxlol.remindme.models

import android.location.Location
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Appointment(
    @ColumnInfo(name = "appointment_id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val text: String?,
    @Embedded val location: LocationMarker?,
    val created: Date,
    val time: Date?,
    val done: Boolean = false,
    val snooze: Date?
){
    fun isInRange(currentLocation : Location, preferenceDistance: Float) : Boolean{
        val distance = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            this.location!!.location.latitude,
            this.location.location.longitude,
            distance
        )
        Log.d("Appointment", "range in meters: ${distance[0]}")
        return distance[0] < preferenceDistance
    }
}