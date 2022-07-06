package com.example.ceroxlol.remindme.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class AppointmentKT(
    @ColumnInfo(name = "appointment_id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val name: String,
    @ColumnInfo val text: String?,
    @Embedded val location: LocationMarker,
    @ColumnInfo val created: Date,
    @ColumnInfo val time: Date?,
    @ColumnInfo val done: Boolean = false
)