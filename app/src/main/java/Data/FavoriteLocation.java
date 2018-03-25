package Data;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import DatabaseServices.LocationPersister;

/**
 * Created by Ceroxlol on 27.12.2017.
 */

//Class for saving the favorite Location spots

@DatabaseTable
public class FavoriteLocation {

    @DatabaseField(generatedId = true, columnName = "LocationID")
    private int id;

    @DatabaseField(canBeNull = false, persisterClass = LocationPersister.class)
    private android.location.Location location;

    public FavoriteLocation()
    {
        // ORMLite needs a no-arg constructor
    }

    public Location getLocation() {
        return location;
    }
}
