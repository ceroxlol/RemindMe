package com.example.ceroxlol.remindme.models.dao

import androidx.room.*
import com.example.ceroxlol.remindme.models.LocationMarker
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
    fun update(locationMarker: LocationMarker)

    @Delete
    suspend fun delete(locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg locationMarker: LocationMarker)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(locationMarker: LocationMarker)
}