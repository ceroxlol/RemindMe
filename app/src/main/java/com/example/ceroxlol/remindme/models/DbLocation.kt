package com.example.ceroxlol.remindme.models

import com.google.android.gms.maps.model.LatLng

data class DbLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    constructor(latLong: LatLng) : this(latitude = latLong.latitude, longitude = latLong.longitude)
}