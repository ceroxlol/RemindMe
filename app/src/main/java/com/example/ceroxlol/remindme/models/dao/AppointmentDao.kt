package com.example.ceroxlol.remindme.models.dao

import androidx.room.*
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM Appointment")
    fun getAll(): Flow<List<Appointment>>

    @Query("SELECT * FROM Appointment WHERE done = false")
    fun getAllNotDone(): Flow<List<Appointment>>

    @Query("SELECT * FROM Appointment ORDER BY done")
    fun getAllSortedByDone(): Flow<List<Appointment>>

    @Query("SELECT * FROM Appointment WHERE appointment_id = :id")
    fun getById(id: Int): Flow<Appointment?>

    @Query("UPDATE Appointment SET done = 1 WHERE appointment_id = :id")
    fun setAppointmentDoneById(id: Int)

    @Query("UPDATE Appointment SET snooze = :snooze WHERE appointment_id = :id")
    fun setAppointmentSnooze(id: Int, snooze: Date)

    @Query("UPDATE Appointment SET location_marker_id = NULL WHERE location_marker_id = :locationMarkerId")
    suspend fun setLocationMarkerDeleted(locationMarkerId : Int)

    @Query("SELECT appointment_id FROM Appointment where location_marker_id = :locationMarkerId")
    suspend fun getAppointmentIdsByLocationMarkerId(locationMarkerId: Int) : List<Int>

    @Query("UPDATE Appointment SET location_marker_id = :locationMarkerId WHERE appointment_id IN (:appointmentIds)")
    suspend fun setLocationMarkerForAppointmentIds(locationMarkerId: Int, appointmentIds: List<Int>)

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(appointment: Appointment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(vararg appointments: Appointment)

    // AppointmentAndLocationMarker

    @Transaction
    @Query("SELECT * FROM Appointment")
    fun getAppointmentAndLocationMarker(): Flow<List<AppointmentAndLocationMarker>>

    @Transaction
    @Query("SELECT * FROM Appointment ORDER BY location_marker_id ASC")
    fun getAppointmentAndLocationMarkerSortedByLocationMarkerId(): Flow<List<AppointmentAndLocationMarker>>

    @Transaction
    @Query("SELECT * FROM Appointment WHERE appointment_id = :appointmentId")
    fun getAppointmentAndLocationMarkerByAppointmentId(appointmentId: Int): Flow<AppointmentAndLocationMarker?>

    @Transaction
    @Query("SELECT * FROM Appointment WHERE done = false")
    fun getAppointmentAndLocationMarkerNotDone(): Flow<List<AppointmentAndLocationMarker>>
}
