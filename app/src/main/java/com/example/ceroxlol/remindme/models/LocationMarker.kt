package com.example.ceroxlol.remindme.models

import android.location.Address
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class LocationMarker(
    @ColumnInfo(name = "location_id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @Embedded val location: DbLocation,
    @ColumnInfo(name = "location_name") val name: String,
    @Embedded val address: Address? = null
) {
    fun isValid(): Boolean {
        return name.isNotEmpty()
    }
}