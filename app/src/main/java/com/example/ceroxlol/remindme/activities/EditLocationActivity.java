package com.example.ceroxlol.remindme.activities;

import static com.example.ceroxlol.remindme.activities.MainActivity.getDb;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ceroxlol.remindme.R;
import com.example.ceroxlol.remindme.adapters.ArrayAdapterLocationsList;
import com.example.ceroxlol.remindme.models.LocationMarker;

import java.util.ArrayList;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout linearLayoutLocations;
    private ArrayList<LocationMarker> locationMarkers;
    private ArrayAdapter<LocationMarker> locationMarkerArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        locationMarkers = (ArrayList<LocationMarker>) getDb().locationMarkerDao().getAll();
        linearLayoutLocations = findViewById(R.id.linearLayoutEditLocationsLocations);
        locationMarkerArrayAdapter = new ArrayAdapterLocationsList(this, locationMarkers, linearLayoutLocations);

        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (int i = 0; i < locationMarkerArrayAdapter.getCount(); i++) {
            View view = locationMarkerArrayAdapter.getView(i, null, linearLayoutLocations);
            linearLayoutLocations.addView(view);
        }
    }
}
