package com.example.ceroxlol.remindme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import Data.FavoriteLocation;

public class EditLocationActivity extends AppCompatActivity {

    private LinearLayout mLinearLayoutLocations;
    private List<FavoriteLocation> mFavoriteLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mFavoriteLocations = MainActivity.mDatabaseHelper.getFavoriteLocationDao().queryForAll();

        //GUI stuff
        mLinearLayoutLocations = (LinearLayout) findViewById(R.id.LinearLayoutLocations);
        
        fillLinearLayoutLocations();
    }

    private void fillLinearLayoutLocations() {
        for (FavoriteLocation favoriteLocation :
                mFavoriteLocations) {
            
        }
    }
}
