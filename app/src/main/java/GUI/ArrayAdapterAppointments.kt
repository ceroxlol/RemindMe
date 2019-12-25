package GUI

import Data.FavoriteLocation
import android.content.Context
import android.widget.ArrayAdapter

class ArrayAdapterAppointments(context: Context, data: ArrayList<FavoriteLocation>) : ArrayAdapter<FavoriteLocation>(context, 0, data) {
}