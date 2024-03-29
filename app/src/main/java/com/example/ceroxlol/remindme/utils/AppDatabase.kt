package com.example.ceroxlol.remindme.utils

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.converter.DateConverter
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import com.example.ceroxlol.remindme.models.dao.LocationMarkerDao

@Database(
    entities = [Appointment::class, LocationMarker::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun locationMarkerDao(): LocationMarkerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "remind_me_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }
    }
}