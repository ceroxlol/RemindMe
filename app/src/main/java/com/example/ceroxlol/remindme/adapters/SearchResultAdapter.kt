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
    addresses: List<Address>,
    private val onItemClicked: (Address) -> Unit,
) : ArrayAdapter<Address>(context, 0, addresses) {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView ?: inflater.inflate(R.layout.adapter_address_display_item, parent, false)

        getItem(position)!!.also { address ->
            setLayout(view, address)
            view.setOnClickListener {
                onItemClicked(address)
            }
        }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: inflater.inflate(R.layout.search_result_item_location_fragment, parent, false)

        getItem(position)!!.also { address ->
            setLayout(view, address)
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