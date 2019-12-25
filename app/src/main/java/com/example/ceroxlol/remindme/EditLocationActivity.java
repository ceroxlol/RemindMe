package com.example.ceroxlol.remindme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

import Data.FavoriteLocation;
import GUI.ArrayAdapterLocations;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout mLinearLayoutLocations;
    private ArrayList<FavoriteLocation> mLocations;
    private ArrayAdapter<FavoriteLocation> mLocationsArrayAdapter;
    private ScrollView mScrollViewFavoriteLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mLocations = (ArrayList<FavoriteLocation>) MainActivity.mDatabaseHelper.getFavoriteLocationDao().queryForAll();
        mLocationsArrayAdapter = new ArrayAdapterLocations(this, mLocations);
        mLinearLayoutLocations = findViewById(R.id.LinearLayoutEditLocationsLocations);
        mScrollViewFavoriteLocations = findViewById(R.id.scrollViewEditLocationsLocations);
        
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
