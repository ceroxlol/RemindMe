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
    @ColumnInfo(name = "appointment_id") @PrimaryKey(autoGenerate = true) val appointmentId: Int = 0,
    val name: String,
    val text: String?,
    val locationMarkerId: Int,
    val created: Date,
    val time: Date?,
    val done: Boolean = false,
    val snooze: Date?
){
    fun isValid(): Boolean{
        return name.isNotEmpty() && name.isNotBlank()
    }
}