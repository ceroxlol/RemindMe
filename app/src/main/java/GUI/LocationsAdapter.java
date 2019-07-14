package GUI;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.ceroxlol.remindme.EditSingleLocationActivity;
import com.example.ceroxlol.remindme.MainActivity;
import com.example.ceroxlol.remindme.R;

import java.util.ArrayList;

import Data.FavoriteLocation;

public class LocationsAdapter extends ArrayAdapter<FavoriteLocation> {
    private Context context;

    public LocationsAdapter(Context context, ArrayList<FavoriteLocation> data)
    {
        super(context, 0, data);
        this.context = context;
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
        TextView favoriteLocationName = (TextView) view.findViewById(R.id.textViewFavoriteLocation);
        Button favoriteLocationEdit = (Button) view.findViewById(R.id.buttonEditFavoriteLocation);
        Button favoriteLocationDelete = (Button) view.findViewById(R.id.buttonDeleteFavoriteLocation);
        // Populate the data into the template view using the data object
        favoriteLocationName.setText(favoriteLocation.getName());
        favoriteLocationEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), EditSingleLocationActivity.class);
                i.putExtra("FavoriteLocationID", favoriteLocation.getID());
                context.startActivity(i);
            }
        });
        favoriteLocationDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.mDatabaseHelper.getFavoriteLocationDao().deleteById(favoriteLocation.getID());

            }
        });

        // Return the completed view to render on screen
        return view;
    }
}

