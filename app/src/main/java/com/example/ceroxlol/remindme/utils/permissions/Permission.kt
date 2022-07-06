package com.example.ceroxlol.remindme.utils.permissions

import android.Manifest.permission.*

sealed class Permission(vararg val permissions: String) {
        // Grouped permissions
    object Location : Permission(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
    object Storage : Permission(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)

    companion object {
        fun from(permission: String) = when (permission) {
            ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> Location
            WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE -> Storage
            else -> throw IllegalArgumentException("Unknown permission: $permission")
        }
    }
}