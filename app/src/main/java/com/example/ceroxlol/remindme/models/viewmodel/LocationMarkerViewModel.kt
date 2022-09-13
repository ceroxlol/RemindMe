package com.example.ceroxlol.remindme.models.viewmodel

import androidx.lifecycle.*
import com.example.ceroxlol.remindme.models.DbLocation
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.dao.LocationMarkerDao
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class LocationMarkerViewModel(private val locationMarkerDao: LocationMarkerDao) : ViewModel() {

    val allLocations: LiveData<List<LocationMarker>> = locationMarkerDao.getAll().asLiveData()

    private fun insertLocationMarker(locationMarker: LocationMarker) {
        viewModelScope.launch {
            locationMarkerDao.insert(locationMarker)
        }
    }

    fun addNewLocationMarker(
        locationMarker: LocationMarker
    ) {
        insertLocationMarker(locationMarker)
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

    fun isEntryValid(name: String, latLng: LatLng?): Boolean {
        if (name.isBlank() || latLng == null) {
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
        val updatedLocationMarker = createNewLocationMarkerFromId(id, name, longitude, latitude)
        updateLocationMarker(updatedLocationMarker)
    }

    private fun updateLocationMarker(locationMarker: LocationMarker) {
        viewModelScope.launch {
            locationMarkerDao.update(locationMarker)
        }
    }

    private fun createNewLocationMarkerFromId(
        id : Int,
        name: String,
        longitude: Double,
        latitude: Double
    ): LocationMarker {
        val dbLocation = DbLocation(latitude, longitude)
        return LocationMarker(
            locationMarkerId = id,
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