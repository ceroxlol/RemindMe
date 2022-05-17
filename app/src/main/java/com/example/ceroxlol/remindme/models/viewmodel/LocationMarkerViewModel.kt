package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.LocationMarkerDao
import kotlinx.coroutines.launch

class LocationMarkerViewModel(private val locationMarkerDao: LocationMarkerDao) : ViewModel() {

    val allItems: LiveData<List<LocationMarker>> = locationMarkerDao.getAll().asLiveData()

    private fun insertLocationMarker(locationMarker: LocationMarker) {
        viewModelScope.launch {
            locationMarkerDao.insert(locationMarker)
        }
    }

    fun addNewLocationMarker(
        name: String,
        dbLocation: DbLocation
    ) {
        val newLocationMarker = getLocationMarkerEntry(name, dbLocation)
        insertLocationMarker(newLocationMarker)
    }

    private fun getLocationMarkerEntry(
        name: String,
        dbLocation: DbLocation
    ): LocationMarker {
        return LocationMarker(
            name = name,
            location = dbLocation
        )
    }

    fun retrieveLocationMarker(id: Int): LiveData<LocationMarker> {
        return locationMarkerDao.getById(id).asLiveData()
    }

    fun isEntryValid(name: String, longitude : Double, latitude : Double): Boolean {
        if (name.isBlank()) {
            return false
        }
        return true
    }

    fun updateLocationMarker(
        id: Int,
        name: String,
        longitude: Double,
        latitude: Double
    ) {
        val updatedLocationMarker = getUpdatedLocationMarkerEntry(id, name, longitude, latitude)
        updateLocationMarker(updatedLocationMarker)
    }

    private fun updateLocationMarker(locationMarker: LocationMarker) {
        viewModelScope.launch {
            locationMarkerDao.update(locationMarker)
        }
    }

    private fun getUpdatedLocationMarkerEntry(
        id : Int,
        name: String,
        longitude: Double,
        latitude: Double
    ): LocationMarker {
        val dbLocation = DbLocation(latitude, longitude)
        return LocationMarker(
            id = id,
            name = name,
            location = dbLocation
        )
    }

    fun deleteLocationMarker(locationMarker: LocationMarker) {
        viewModelScope.launch {
            locationMarkerDao.delete(locationMarker)
        }
    }
}

class LocationMarkerViewModelFactory(private val locationMarkerDao: LocationMarkerDao) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationMarkerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationMarkerViewModel(locationMarkerDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}