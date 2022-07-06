package com.example.ceroxlol.remindme.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.models.LocationMarker

class ArrayAdapterLocationsListSpinner(context: Context, data: List<LocationMarker>?) :
    ArrayAdapter<LocationMarker>(context, 0, data!!) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //TODO: Beautify
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView ?: inflater.inflate(R.layout.spinner_item_location, parent, false)

        getItem(position)?.let {
            val tv = view.findViewById<TextView>(R.id.textViewLocationName)
            tv.text = it.name
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.spinner_item_location, parent, false)

        getItem(position)?.let {
            val tv = view.findViewById<TextView>(R.id.textViewLocationName)
            tv.text = it.name
        }

        return view
    }
}