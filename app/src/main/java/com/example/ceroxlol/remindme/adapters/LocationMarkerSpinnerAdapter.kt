package com.example.ceroxlol.remindme.adapters

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.models.LocationMarker

/**
 * [LocationMarkerSpinnerAdapter] implementation scrollable locations to be attached to an appointment.
 */
class LocationMarkerSpinnerAdapter(context: Context, data: List<LocationMarker>) :
    ArrayAdapter<LocationMarker>(context, 0, data) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    //TODO: Beautify
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View =
            convertView ?: inflater.inflate(R.layout.spinner_location_display_item, parent, false)

        getItem(position)?.let {
            val tv = view.findViewById<TextView>(R.id.textViewSpinnerLocationMarkerSingleItem)
            tv.text = it.name

            if (it.name.count() in 22..32) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            } else if (it.name.count() > 32) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View =
            convertView ?: inflater.inflate(R.layout.spinner_dropdown_item_location, parent, false)

        getItem(position)?.let {
            val tv = view.findViewById<TextView>(R.id.textViewSpinnerLocationMarkerSingleItem)
            tv.text = it.name

            if (it.name.count() in 22..32) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            } else if (it.name.count() > 32) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
        }

        return view
    }
}