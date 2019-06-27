package DatabaseServices;

import android.location.Location;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.SQLException;

import static DatabaseServices.DatabaseHelper.gson;

/**
 * Created by Ceroxlol on 21.12.2017.
 */

public class LocationPersister extends StringType{

    private static final LocationPersister instance = new LocationPersister();


    private LocationPersister(){
        super(SqlType.LONG_STRING, new Class<?>[]{Location.class});
    }

    public static LocationPersister getSingleton(){
        return instance;
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
        if (javaObject == null) {
            return null;
        } else {
            return gson.toJson(javaObject, Location.class);
        }
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        /*String to_restore_object = gson.toJson(sqlArg, LocationHandler.class);
        if (to_restore_object == null) {
            return null;
        } else {*/
            //LocationHandler location = gson.fromJson(to_restore_object, LocationHandler.class);
            Location location = gson.fromJson(sqlArg.toString(), Location.class);
            return location;
        //}
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String s) {
        return s;
    }

}
