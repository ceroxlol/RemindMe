package com.example.ceroxlol.remindme.utils

import android.location.Location
import com.example.ceroxlol.remindme.models.DbLocation
import com.google.android.gms.maps.model.LatLng

fun Location.toLatLng() : LatLng {
    return LatLng(this.latitude, this.longitude)
}

fun DbLocation.toLatLng() : LatLng{
    return LatLng(this.latitude, this.longitude)
}