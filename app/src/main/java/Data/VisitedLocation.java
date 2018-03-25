package Data;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import DatabaseServices.LocationPersister;

/**
 * Created by Ceroxlol on 27.02.2018.
 */

@DatabaseTable
public class VisitedLocation {
    @DatabaseField(generatedId = true, columnName = "LocationID")
    private int id;

    @DatabaseField(canBeNull = false, persisterClass = LocationPersister.class)
    private android.location.Location location;

    public VisitedLocation()
    {
        // ORMLite needs a no-arg constructor
    }

    public Location getLocation() {
        return location;
    }
}
