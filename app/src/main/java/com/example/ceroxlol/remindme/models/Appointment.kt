package com.example.ceroxlol.remindme.models

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
)