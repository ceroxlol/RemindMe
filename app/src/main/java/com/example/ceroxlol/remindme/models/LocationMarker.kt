package com.example.ceroxlol.remindme.models

import android.location.Location
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ceroxlol.remindme.utils.isValidForPersistence
import com.google.android.gms.maps.model.LatLng

@Entity
data class LocationMarker(
    @ColumnInfo(name = "location_id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded val location: DbLocation,
    @ColumnInfo(name = "location_name") val name: String,
    @ColumnInfo(name = "location_address") val address: String? = null
) {
    fun isValid(): Boolean {
        return name.isValidForPersistence()
    }

    fun isEntryValid(): Boolean =
        this.name.isNotBlank()

    fun isInRange(currentLocation: Location, preferenceDistance: Float): Boolean {
        val distance = FloatArray(1)
        Location.distanceBetween(
            currentLocation.latitude,
            currentLocation.longitude,
            this.location.latitude,
            this.location.longitude,
            distance
        )
        Log.d("Appointment", "range in meters: ${distance[0]}")
        return distance[0] < preferenceDistance
    }
}