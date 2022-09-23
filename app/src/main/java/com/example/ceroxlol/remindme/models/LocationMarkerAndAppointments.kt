package com.example.ceroxlol.remindme.models

import androidx.room.Embedded
import androidx.room.Relation

data class LocationMarkerAndAppointments(
    @Embedded val locationMarker: LocationMarker,

    @Relation(
        parentColumn = "location_id",
        entity = Appointment::class,
        entityColumn = "location_marker_id")
    val appointments: List<Appointment>
)
