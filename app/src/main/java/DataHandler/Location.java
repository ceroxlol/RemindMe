package DataHandler;

import java.io.Serializable;

/**
 * Created by Ceroxlol on 28.12.2017.
 */

public class Location implements Serializable {
    private android.location.Location location;

    //default ctor
    public Location()
    {

    }

    public Location(android.location.Location location)
    {
        this.location = location;
    }

    //Cast operator Favorite Location
    public static final Location DataHandlerLocationToDataLocation(final Data.FavoriteLocation location_to_be_casted){
        return new Location(location_to_be_casted.getLocation());
    }

    //Cast operator Visited Location
    public static final Location DataHandlerLocationToDataLocation(final Data.VisitedLocation location_to_be_casted){
        return new Location(location_to_be_casted.getLocation());
    }
}
