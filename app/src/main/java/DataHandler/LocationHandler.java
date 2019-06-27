package DataHandler;

import java.io.Serializable;

/**
 * Created by Ceroxlol on 28.12.2017.
 */

public class LocationHandler implements Serializable {
    private android.location.Location location;

    //default ctor
    public LocationHandler()
    {

    }

    public LocationHandler(android.location.Location location)
    {
        this.location = location;
    }

    //Cast operator Favorite LocationHandler
    public static final LocationHandler DataHandlerLocationToDataLocation(final Data.FavoriteLocation location_to_be_casted){
        return new LocationHandler(location_to_be_casted.getLocation());
    }

    //Cast operator Visited LocationHandler
    public static final LocationHandler DataHandlerLocationToDataLocation(final Data.VisitedLocation location_to_be_casted){
        return new LocationHandler(location_to_be_casted.getLocation());
    }
}
