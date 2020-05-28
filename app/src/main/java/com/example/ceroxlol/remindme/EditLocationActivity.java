package com.example.ceroxlol.remindme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        mLocationsArrayAdapter = new ArrayAdapterLocationsList(this, mLocations);
        mLinearLayoutLocations = findViewById(R.id.linearLayoutEditLocationsLocations);
        
        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (int i = 0; i < mLocationsArrayAdapter.getCount(); i++)
        {
            View view = mLocationsArrayAdapter.getView(i, null, null);
            mLinearLayoutLocations.addView(view);
        }
    }
}
