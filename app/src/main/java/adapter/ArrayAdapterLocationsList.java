package adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ceroxlol.remindme.EditSingleLocationActivity;
import com.example.ceroxlol.remindme.MainActivity;
import com.example.ceroxlol.remindme.R;

import java.util.ArrayList;

import Data.FavoriteLocation;

public class ArrayAdapterLocationsList extends ArrayAdapter<FavoriteLocation> {
    private final LinearLayout linearLayoutLocations;
    private Context context;
    private ArrayList<FavoriteLocation> locations;

    public ArrayAdapterLocationsList(Context context, ArrayList<FavoriteLocation> data, LinearLayout linearLayoutLocations)
    {
        super(context, 0, data);
        this.context = context;
        this.locations = data;
        this.linearLayoutLocations = linearLayoutLocations;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        // Get the data item for this position
        final FavoriteLocation favoriteLocation = getItem(i);
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_locations_list, parent, false);
        }
        // Lookup view for data population
        TextView favoriteLocationName = view.findViewById(R.id.textViewFavoriteLocation);
        Button favoriteLocationDelete = view.findViewById(R.id.buttonDeleteFavoriteLocation);
        // Populate the data into the template view using the data object
        favoriteLocationName.setText(favoriteLocation.getName());
        View finalView = view;
        favoriteLocationDelete.setOnClickListener(v ->
                {
                    this.locations.remove(favoriteLocation);
                    this.linearLayoutLocations.removeView(finalView);
                    MainActivity.mDatabaseHelper.getFavoriteLocationDaoRuntimeException().deleteById(favoriteLocation.getID());
                }
        );

        // Return the completed view to render on screen
        return view;
    }
}

