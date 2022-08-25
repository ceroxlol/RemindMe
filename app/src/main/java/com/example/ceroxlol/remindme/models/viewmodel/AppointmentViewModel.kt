package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.Appointment
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.AppointmentDao
import kotlinx.coroutines.launch
import java.util.*

class AppointmentViewModel(private val appointmentDao: AppointmentDao) : ViewModel() {

    val allAppointments: LiveData<List<Appointment>> = appointmentDao.getAll().asLiveData()
    val allAppointmentsSortedByDone: LiveData<List<Appointment>> = appointmentDao.getAllSortedByDone().asLiveData()

    private fun insertAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.insert(appointment)
        }
    }

    fun addNewAppointment(appointment: Appointment){
        insertAppointment(appointment)
    }

    fun addNewAppointment(
        name: String,
        text: String?,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ) {
        val newAppointment = createAppointmentInstance(name, text, locationMarker, time, done)
        insertAppointment(newAppointment)
    }

    private fun createAppointmentInstance(
        name: String,
        text: String?,
        locationMarker: LocationMarker,
        time: Date?,
        done: Boolean
    ): Appointment {
        return Appointment(
            name = name,
            text = text,
            location = locationMarker,
            created = Calendar.getInstance().time,
            time = time,
            done = done,
            snooze = null
        )
    }

    fun retrieveAppointment(id: Int): LiveData<Appointment> {
        return appointmentDao.getById(id).asLiveData()
    }

    fun isEntryValid(appointmentName: String, appointmentText: String, appointmentLocation: LocationMarker): Boolean {
        //TODO: Doesn't catch empty
        if (appointmentName.isNotBlank() || appointmentText.isNotBlank() || appointmentLocation.isValid()) {
            return true
        }
        return false
    }

    fun updateAppointment(
        appointmentId: Int,
        appointmentName: String,
        appointmentText: String,
        appointmentLocation: LocationMarker
    ) {
        val updatedItem = getUpdatedAppointmentEntry(appointmentId, appointmentName, appointmentText, appointmentLocation)
        updateAppointment(updatedItem)
    }

    private fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentDao.update(appointment)
        }
    }

    private fun getUpdatedAppointmentEntry(
        appointmentId : Int,
        appointmentName: String,
        appointmentText: String,
        appointmentLocation: LocationMarker
    ): Appointment {
        return Appointment(
            id = appointmentId,
            name = appointmentName,
            text = appointmentText,
            location = appointmentLocation,
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