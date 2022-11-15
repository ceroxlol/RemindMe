package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import kotlinx.coroutines.launch
import java.util.*

class AppointmentViewModel(private val appointmentDao: AppointmentDao) : ViewModel() {

    fun appointmentAndLocationMarkerByAppointmentId(appointmentId: Int): LiveData<AppointmentAndLocationMarker?> {
        return appointmentDao.getAppointmentAndLocationMarkerByAppointmentId(appointmentId)
            .asLiveData()
    }

    private fun insertAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.upsert(appointment)
        }
    }

    private fun upsertAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.upsert(appointment)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.delete(appointment)
        }
    }

    fun saveAppointment(appointment: Appointment) {
        upsertAppointment(appointment)
    }

    fun addNewAppointment(appointment: Appointment) {
        insertAppointment(appointment)
    }

}

class AppointmentViewModelFactory(private val appointmentDao: AppointmentDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(appointmentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}