package com.example.ceroxlol.remindme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ceroxlol.remindme.activities.MainActivity;
import com.example.ceroxlol.remindme.R;

import java.util.ArrayList;

import com.example.ceroxlol.remindme.models.FavoriteLocation;

public class ArrayAdapterLocationsList extends ArrayAdapter<FavoriteLocation> {
    private final LinearLayout linearLayoutLocations;
    private final ArrayList<FavoriteLocation> locations;

    public ArrayAdapterLocationsList(Context context, ArrayList<FavoriteLocation> data, LinearLayout linearLayoutLocations)
    {
        super(context, 0, data);
        this.locations = data;
        this.linearLayoutLocations = linearLayoutLocations;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        final FavoriteLocation favoriteLocation = getItem(i);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_locations_list, parent, false);
        }
        TextView favoriteLocationName = view.findViewById(R.id.textViewFavoriteLocation);
        Button favoriteLocationDelete = view.findViewById(R.id.buttonDeleteFavoriteLocation);
        favoriteLocationName.setText(favoriteLocation.getName());
        View finalView = view;
        favoriteLocationDelete.setOnClickListener(v ->
                {
                    this.locations.remove(favoriteLocation);
                    this.linearLayoutLocations.removeView(finalView);
                    MainActivity.databaseHelper.getFavoriteLocationDaoRuntimeException().deleteById(favoriteLocation.getId());
                }
        );

        return view;
    }
}

