package com.example.ceroxlol.remindme.models

import android.location.Location
import android.util.Log
import androidx.room.Embedded
import androidx.room.Relation

data class LocationMarkerWithAppointments(
    @Embedded val locationMarker: LocationMarker,
    @Relation(
        parentColumn = "locationId",
        entityColumn = "locationMarkerId"
    )
    val appointments: List<Appointment>
)
