package com.example.ceroxlol.remindme.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocationMarker(
    @ColumnInfo(name = "location_id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded val location: DbLocation,
    @ColumnInfo(name = "location_name") val name: String
) {
    fun isValid(): Boolean {
        return name.isNotEmpty()
    }
}