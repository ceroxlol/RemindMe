package com.example.ceroxlol.remindme;

import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Data.FavoriteLocation;
import GUI.LocationsAdapter;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout mLinearLayoutLocations;
    private ArrayList<FavoriteLocation> mFavoriteLocations;
    private ArrayAdapter<FavoriteLocation> mFavoriteLocationsArrayAdapter;
    private ScrollView mScrollViewFavoriteLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mFavoriteLocations = (ArrayList<FavoriteLocation>) MainActivity.mDatabaseHelper.getFavoriteLocationDao().queryForAll();
        mFavoriteLocationsArrayAdapter = new LocationsAdapter(this, mFavoriteLocations);
        //GUI stuff
        mLinearLayoutLocations = (LinearLayout) findViewById(R.id.LinearLayoutLocations);
        mScrollViewFavoriteLocations = (ScrollView) findViewById(R.id.scrollViewFavoriteLocations);


        
        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (int i=0; i < mFavoriteLocationsArrayAdapter.getCount(); i++)
        {
            View view = mFavoriteLocationsArrayAdapter.getView(i, null, null);
            mLinearLayoutLocations.addView(view);
        }
    }
}
