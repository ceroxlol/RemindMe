package com.example.ceroxlol.remindme

import android.app.Application
import com.example.ceroxlol.remindme.activities.MainActivity
import com.example.ceroxlol.remindme.utils.AppDatabase

class RemindMeApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}