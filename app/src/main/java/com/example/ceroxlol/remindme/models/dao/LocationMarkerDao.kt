package com.example.ceroxlol.remindme.models.dao

import android.location.Location
import androidx.room.*
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.LocationMarkerAndAppointments
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationMarkerDao {
    @Query("SELECT * FROM LocationMarker")
    fun getAll(): Flow<List<LocationMarker>>

    @Query("SELECT * FROM LocationMarker WHERE location_id = :id")
    fun getById(id: Int): Flow<LocationMarker>

    @Query("SELECT EXISTS(SELECT * FROM LocationMarker)")
    fun entriesExist() : Boolean

    @Update
    suspend fun update(locationMarker: LocationMarker)

    @Delete
    suspend fun delete(locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationMarker: LocationMarker)

    @Transaction
    @Query("SELECT * FROM LocationMarker")
    fun getLocationMarkerAndAppointments(): Flow<List<LocationMarkerAndAppointments>>
}