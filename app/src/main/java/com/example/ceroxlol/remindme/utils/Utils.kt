package com.example.ceroxlol.remindme.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Location
import androidx.preference.PreferenceManager
import com.example.ceroxlol.remindme.R
import com.google.android.gms.maps.GoogleMap
import java.text.DateFormat
import java.util.*


object Utils {

    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun requestingLocationUpdates(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
            .apply()
    }

    /**
     * Returns the `location` object as a human readable string.
     * @param location  The [Location].
     */
    fun getLocationText(location: Location?): String {
        return if (location == null) "Unknown location"
            else "(" + location.latitude.toString() + ", " + location.longitude.toString() + ")"
    }

    fun getLocationTitle(context: Context): String {
        return context.getString(
            R.string.location_updated,
            DateFormat.getDateTimeInstance().format(Date())
        )
    }
}

fun String.isValidForPersistence() : Boolean{
    if(this.isBlank() || this.isEmpty() || this == ""){
        return false
    }
    return true
}

//TODO: Set the correct address here
fun Address.getHumanReadableAddress(): String {
    return this.featureName + ", " + this.adminArea + ", " + this.countryName
}

@SuppressLint("MissingPermission")
fun GoogleMap.setLocationEnabled(enabled: Boolean){
    this.isMyLocationEnabled = enabled
    this.uiSettings.isMyLocationButtonEnabled = enabled
}