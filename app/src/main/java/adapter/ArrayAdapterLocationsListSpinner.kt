package adapter

import Data.FavoriteLocation
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ceroxlol.remindme.R

class ArrayAdapterLocationsListSpinner(context: Context, data: ArrayList<FavoriteLocation>?) : ArrayAdapter<FavoriteLocation>(context, 0, data!!) {

    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val location = getItem(position)

        val rowView = inflater.inflate(R.layout.spinner_text_view_locations, parent, false)

        val textViewSpinnerLocation : TextView = rowView.findViewById(R.id.textView_spinner_location)

        textViewSpinnerLocation.text = location!!.name

        return rowView
    }
}