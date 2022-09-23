package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.ceroxlol.remindme.models.dao.AppointmentDao

class AppointmentAndLocationMarkerViewModel(val appointmentDao: AppointmentDao) :
    ViewModel() {

    val getAllSortedByLocationMarkerId =
        appointmentDao.getAppointmentAndLocationMarkerSortedByLocationMarkerId().asLiveData()
}

class AppointmentAndLocationMarkerViewModelFactory(private val appointmentDao: AppointmentDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentAndLocationMarkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentAndLocationMarkerViewModel(appointmentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}