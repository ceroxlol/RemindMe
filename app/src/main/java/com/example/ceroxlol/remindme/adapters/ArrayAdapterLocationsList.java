package com.example.ceroxlol.remindme.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ceroxlol.remindme.R;

import java.util.ArrayList;

import com.example.ceroxlol.remindme.models.LocationMarker;

public class ArrayAdapterLocationsList extends ArrayAdapter<LocationMarker> {
    private final LinearLayout linearLayoutLocations;
    private final ArrayList<LocationMarker> locationMarkers;

    public ArrayAdapterLocationsList(Context context, ArrayList<LocationMarker> data, LinearLayout linearLayoutLocations)
    {
        super(context, 0, data);
        this.locationMarkers = data;
        this.linearLayoutLocations = linearLayoutLocations;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        final LocationMarker item = getItem(i);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_locations_list, parent, false);
        }
        TextView favoriteLocationName = view.findViewById(R.id.textViewFavoriteLocation);
        Button favoriteLocationDelete = view.findViewById(R.id.buttonDeleteFavoriteLocation);
        favoriteLocationName.setText(item.getName());
        View finalView = view;
        favoriteLocationDelete.setOnClickListener(v ->
                {
                    this.locationMarkers.remove(item);
                    this.linearLayoutLocations.removeView(finalView);
                    //TODO
                    //getDb().locationMarkerDao().delete(item);
                }
        );

        return view;
    }
}

