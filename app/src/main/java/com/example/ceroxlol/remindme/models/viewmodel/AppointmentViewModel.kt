package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import com.example.ceroxlol.remindme.utils.isValidForPersistence
import kotlinx.coroutines.launch
import java.util.*

class AppointmentViewModel(private val appointmentDao: AppointmentDao) : ViewModel() {

    val appointmentAndLocationMarker = appointmentDao.getAppointmentAndLocationMarker().asLiveData()

    fun appointmentAndLocationMarkerByAppointmentId(appointmentId: Int): LiveData<AppointmentAndLocationMarker?> {
        return appointmentDao.getAppointmentAndLocationMarkerByAppointmentId(appointmentId)
            .asLiveData()
    }

    private fun insertAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.upsert(appointment)
        }
    }

    private fun upsertAppointment(appointment: Appointment){
        viewModelScope.launch {
            appointmentDao.upsert(appointment)
        }
    }

    fun saveAppointment(appointment: Appointment){
        upsertAppointment(appointment)
    }

    fun addNewAppointment(appointment: Appointment) {
        insertAppointment(appointment)
    }

    fun addNewAppointment(
        name: String,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ) {
        val newAppointment = createAppointmentInstance(name, locationMarker, time, done)
        insertAppointment(newAppointment)
    }

    private fun createAppointmentInstance(
        name: String,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ): Appointment {
        return Appointment(
            name = name,
            locationMarkerId = locationMarker.id,
            created = Calendar.getInstance().time,
            time = time,
            done = done,
            snooze = null
        )
    }

    //TODO Delete
    fun isEntryValid(appointmentName: String, appointmentLocation: LocationMarker): Boolean {
        return appointmentName.isValidForPersistence() && appointmentLocation.isValid()
    }

    fun updateAppointment(
        appointmentId: Int,
        appointmentName: String,
        appointmentLocation: LocationMarker
    ) {
        val updatedItem = getUpdatedAppointmentEntry(
            appointmentId,
            appointmentName,
            appointmentLocation
        )
        updateAppointment(updatedItem)
    }

    private fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.update(appointment)
        }
    }

    private fun getUpdatedAppointmentEntry(
        appointmentId: Int,
        appointmentName: String,
        appointmentLocation: LocationMarker
    ): Appointment {
        return Appointment(
            id = appointmentId,
            name = appointmentName,
            locationMarkerId = appointmentLocation.id,
            created = Calendar.getInstance().time,
            done = false,
            time = null,
            snooze = null
        )
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.delete(appointment)
        }
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