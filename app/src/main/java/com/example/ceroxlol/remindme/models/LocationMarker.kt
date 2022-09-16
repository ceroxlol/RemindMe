package com.example.ceroxlol.remindme.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ceroxlol.remindme.utils.isValidForPersistence

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
}