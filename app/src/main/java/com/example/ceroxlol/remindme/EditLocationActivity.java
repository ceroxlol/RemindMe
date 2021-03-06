package com.example.ceroxlol.remindme;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;

import adapter.ArrayAdapterLocationsList;
import Data.FavoriteLocation;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout mLinearLayoutLocations;
    private ArrayList<FavoriteLocation> mLocations;
    private ArrayAdapter<FavoriteLocation> mLocationsArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mLocations = (ArrayList<FavoriteLocation>) MainActivity.mDatabaseHelper.getFavoriteLocationDaoRuntimeException().queryForAll();
        mLinearLayoutLocations = findViewById(R.id.linearLayoutEditLocationsLocations);
        mLocationsArrayAdapter = new ArrayAdapterLocationsList(this, mLocations, mLinearLayoutLocations);

        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (int i = 0; i < mLocationsArrayAdapter.getCount(); i++) {
            View view = mLocationsArrayAdapter.getView(i, null, mLinearLayoutLocations);
            mLinearLayoutLocations.addView(view);
        }
    }
}
