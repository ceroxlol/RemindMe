package com.example.ceroxlol.remindme.models.dao

import androidx.room.*
import com.example.ceroxlol.remindme.models.AppointmentKT
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM AppointmentKT")
    fun getAll(): Flow<List<AppointmentKT>>

    @Query("SELECT * FROM AppointmentKT WHERE appointment_id = :id")
    fun getById(id: Int): Flow<AppointmentKT>

    @Query("UPDATE AppointmentKT SET done = 'true' WHERE appointment_id = :id")
    fun setAppointmentDoneById(id:Int)

    @Update
    fun update(appointment: AppointmentKT)

    @Delete
    suspend fun delete(appointment: AppointmentKT)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(appointment: AppointmentKT)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg appointments: AppointmentKT)
}
