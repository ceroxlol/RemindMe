package com.example.ceroxlol.remindme.models;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import com.example.ceroxlol.remindme.utils.LocationPersister;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Ceroxlol on 27.12.2017.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable
public class FavoriteLocation {

    @DatabaseField(generatedId = true, columnName = "LocationID")
    private int id;

    @DatabaseField(canBeNull = false, persisterClass = LocationPersister.class)
    private android.location.Location location;

    @DatabaseField(canBeNull = false)
    private String name;

    public FavoriteLocation(Location location) {
        this.location = location;
    }

    public FavoriteLocation(LatLng latLng, String name) {
        //Provider name is unnecessary
        this.location = new Location("");
        this.location.setLatitude(latLng.latitude);
        this.location.setLongitude(latLng.longitude);
        this.name = name;
    }
}
