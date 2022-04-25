package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import kotlinx.coroutines.launch
import java.util.*

class AppointmentKTViewModel(private val appointmentDao: AppointmentDao) : ViewModel() {
    private fun insertAppointmentKT(appointmentKT: AppointmentKT) {
        viewModelScope.launch {
            appointmentDao.insert(appointmentKT)
        }
    }

    private fun addNewAppointmentKT(name: String, text: String?, locationMarker: LocationMarker, time: Date?, done: Boolean) {
        val newAppointmentKT = getNewAppointmentKTEntry(name, text, locationMarker, time, done)
        insertAppointmentKT(newAppointmentKT)
    }

    private fun getNewAppointmentKTEntry(name: String, text: String?, locationMarker: LocationMarker, time: Date?, done: Boolean): AppointmentKT {
        return AppointmentKT(
            name = name,
            text = text,
            location = locationMarker,
            time = time,
            done = done
        )
    }
}

class AppointmentKTViewModelFactory(private val appointmentDao: AppointmentDao) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentKTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentKTViewModel(appointmentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}