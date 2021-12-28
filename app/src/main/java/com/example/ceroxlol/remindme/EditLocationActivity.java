package com.example.ceroxlol.remindme;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import Data.FavoriteLocation;
import adapter.ArrayAdapterLocationsList;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout linearLayoutLocations;
    private ArrayList<FavoriteLocation> FavoriteLocations;
    private ArrayAdapter<FavoriteLocation> locationsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        FavoriteLocations = (ArrayList<FavoriteLocation>) MainActivity.mDatabaseHelper.getFavoriteLocationDaoRuntimeException().queryForAll();
        linearLayoutLocations = findViewById(R.id.linearLayoutEditLocationsLocations);
        locationsArrayAdapter = new ArrayAdapterLocationsList(this, FavoriteLocations, linearLayoutLocations);

        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (int i = 0; i < locationsArrayAdapter.getCount(); i++) {
            View view = locationsArrayAdapter.getView(i, null, linearLayoutLocations);
            linearLayoutLocations.addView(view);
        }
    }
}
