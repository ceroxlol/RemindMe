package com.example.ceroxlol.remindme.models

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationMarker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val location: Location,
    @ColumnInfo val name: String
)
