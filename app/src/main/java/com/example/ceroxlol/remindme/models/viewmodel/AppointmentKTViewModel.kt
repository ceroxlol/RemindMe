package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import kotlinx.coroutines.launch
import java.util.*

class AppointmentKTViewModel(private val appointmentDao: AppointmentDao) : ViewModel() {

    val allItems: LiveData<List<AppointmentKT>> = appointmentDao.getAll().asLiveData()

    private fun insertAppointmentKT(appointmentKT: AppointmentKT) {
        viewModelScope.launch {
            appointmentDao.insert(appointmentKT)
        }
    }

    fun addNewAppointmentKT(
        name: String,
        text: String?,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ) {
        val newAppointmentKT = getAppointmentKTEntry(name, text, locationMarker, time, done)
        insertAppointmentKT(newAppointmentKT)
    }

    private fun getAppointmentKTEntry(
        name: String,
        text: String?,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ): AppointmentKT {
        return AppointmentKT(
            name = name,
            text = text,
            location = locationMarker,
            created = Date(System.currentTimeMillis()).time,
            time = time,
            done = done
        )
    }

    fun retrieveAppointmentKt(id: Int): LiveData<AppointmentKT> {
        return appointmentDao.getById(id).asLiveData()
    }

    fun isEntryValid(appointmentName: String, appointmentText: String, appointmentLocation: LocationMarker): Boolean {
        if (appointmentName.isBlank() || appointmentText.isBlank() || appointmentLocation.isValid()) {
            return false
        }
        return true
    }

    fun updateItem(
        appointmentKTId: Int,
        appointmentKTName: String,
        appointmentKTText: String,
        appointmentKTLocation: LocationMarker
    ) {
        val updatedItem = getUpdatedAppointmentEntry(appointmentKTId, appointmentKTName, appointmentKTText, appointmentKTLocation)
        updateItem(updatedItem)
    }

    private fun updateItem(appointmentKT: AppointmentKT) {
        viewModelScope.launch {
            appointmentDao.update(appointmentKT)
        }
    }

    private fun getUpdatedAppointmentEntry(
        appointmentKTId : Int,
        appointmentKTName: String,
        appointmentKTText: String,
        appointmentKTLocation: LocationMarker
    ): AppointmentKT {
        return AppointmentKT(
            id = appointmentKTId,
            name = appointmentKTName,
            text = appointmentKTText,
            location = appointmentKTLocation,
            created = Date(System.currentTimeMillis()).time,
            done = false,
            time = null
        )
    }
}

class AppointmentKTViewModelFactory(private val appointmentDao: AppointmentDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentKTViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentKTViewModel(appointmentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}