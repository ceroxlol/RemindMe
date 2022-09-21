package com.example.ceroxlol.remindme.models

import androidx.room.Embedded
import androidx.room.Relation

data class AppointmentAndLocationMarker(
    @Embedded val appointment: Appointment,

    @Relation(
        parentColumn = "locationMarkerId",
        entity = LocationMarker::class,
        entityColumn = "location_id")
    val locationMarker: LocationMarker
)
