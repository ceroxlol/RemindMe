package Data;

import android.location.Location;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import DatabaseServices.LocationPersister;

/**
 * Created by Ceroxlol on 27.12.2017.
 */

//Class for saving the favorite LocationHandler spots

@DatabaseTable
public class FavoriteLocation {

    @DatabaseField(generatedId = true, columnName = "LocationID")
    private int id;

    @DatabaseField(canBeNull = false, persisterClass = LocationPersister.class)
    private android.location.Location location;

    @DatabaseField(canBeNull = false)
    private String name;

    public FavoriteLocation()
    {
        // ORMLite needs a no-arg constructor
    }

    public FavoriteLocation(Location location)
    {
        this.location = location;
    }
    public FavoriteLocation(LatLng latLng, String name)
    {
        //Provider name is unnecessary
        this.location = new Location("");
        this.location.setLatitude(latLng.latitude);
        this.location.setLongitude(latLng.longitude);
        this.name = name;
    }

    public FavoriteLocation(LatLng latLng)
    {
        //Provider name is unnecessary
        this.location = new Location("");
        this.location.setLatitude(latLng.latitude);
        this.location.setLongitude(latLng.longitude);
        this.name = "unkown";
    }

    public Location getLocation() {
        return location;
    }

    public String getName(){return name;}

    public Integer getID(){return id;}

    //For displaying the name in the ArrayAdapterLocationsListSpinner class
    public String toString()
    {
        return this.name;
    }
}
