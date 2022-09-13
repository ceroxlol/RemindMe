package com.example.ceroxlol.remindme.models.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.ceroxlol.remindme.models.LocationMarkerWithAppointments

@Dao
interface LocationMarkerWithAppointmentsDao {
    @Transaction
    @Query("SELECT * FROM LocationMarkerWithAppointments")
    fun getLocationMarkersWithAppointments(): List<LocationMarkerWithAppointments>
}