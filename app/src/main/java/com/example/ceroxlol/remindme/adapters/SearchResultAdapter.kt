package com.example.ceroxlol.remindme.adapters

import android.content.Context
import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ceroxlol.remindme.R

class SearchResultAdapter(
    context: Context,
    addresses: List<Address>
) : ArrayAdapter<Address>(context, 0, addresses) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView ?: inflater.inflate(R.layout.address_adapter_item, parent, false)

        getItem(position)?.let {
            setLayout(view, it)
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.spinner_item_location, parent, false)

        getItem(position)?.let {
            setLayout(view, it)
        }

        return view
    }

    private fun setLayout(view: View, address: Address){
        val tvStreet = view.findViewById<TextView>(R.id.textViewAddressStreet)
        val tvCountryAndArea = view.findViewById<TextView>(R.id.textViewAddressCountryAndArea)
        tvStreet.text = address.featureName
        tvCountryAndArea.text = "${address.adminArea}, ${address.countryName}"
    }
}