package com.example.ceroxlol.remindme.models.dao

import androidx.room.*
import com.example.ceroxlol.remindme.models.LocationMarker

@Dao
interface LocationMarkerDao {
    @Query("SELECT * FROM LocationMarker")
    fun getAll(): List<LocationMarker>

    @Query("SELECT * FROM LocationMarker WHERE id = :id")
    fun getById(id: Int): LocationMarker

    @Query("SELECT EXISTS(SELECT * FROM LocationMarker)")
    fun entriesExist() : Boolean

    @Update
    fun updateAppointment(locationMarker: LocationMarker)

    @Delete
    suspend fun delete(locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(locationMarker: LocationMarker)
}