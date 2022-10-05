package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import kotlinx.coroutines.launch

class AppointmentAndLocationMarkerViewModel(private val appointmentDao: AppointmentDao) :
    ViewModel() {

    val getAllSortedByLocationMarkerId =
        appointmentDao.getAppointmentAndLocationMarkerSortedByLocationMarkerId().asLiveData()

    fun getAppointmentIdsByLocationMarkerId(locationMarkerId: Int): LiveData<List<Int>> {
        val result: MutableLiveData<List<Int>> = MutableLiveData()
        viewModelScope.launch {
            result.postValue(appointmentDao.getAppointmentIdsByLocationMarkerId(locationMarkerId))
        }
        return result
    }

    fun setLocationMarkerForAppointmentIds(locationMarkerId: Int, appointmentIds: List<Int>) {
        viewModelScope.launch {
            appointmentDao.setLocationMarkerForAppointmentIds(
                locationMarkerId,
                appointmentIds
            )
        }
    }

    fun deleteLocationMarkerForAppointments(locationMarkerId: Int) {
        viewModelScope.launch {
            appointmentDao.setLocationMarkerDeleted(locationMarkerId)
        }
    }
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