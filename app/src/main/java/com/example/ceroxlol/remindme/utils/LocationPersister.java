package com.example.ceroxlol.remindme.utils;

import static com.example.ceroxlol.remindme.utils.DatabaseHelper.gson;

import android.location.Location;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;

/**
 * Created by Ceroxlol on 21.12.2017.
 */

public class LocationPersister extends StringType {

    private static final LocationPersister instance = new LocationPersister();

    public static LocationPersister getSingleton() {
        return instance;
    }

    private LocationPersister() {
        super(SqlType.LONG_STRING, new Class<?>[]{Location.class});
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
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
        return gson.fromJson(sqlArg.toString(), Location.class);
    }

    @Override
    public Object parseDefaultString(FieldType fieldType, String s) {
        return s;
    }

}
