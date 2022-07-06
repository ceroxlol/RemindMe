package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerListAdapter
import com.example.ceroxlol.remindme.databinding.LocationListFragmentBinding
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory

class LocationsListFragment : Fragment() {

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database.locationMarkerDao()
        )
    }

    private var _binding: LocationListFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LocationListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Beautify items
        val adapter = LocationMarkerListAdapter {
            val action = MainFragmentDirections.actionMainFragmentToEditLocationFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) { locations ->
            locations.let {
                adapter.submitList(it)
            }
        }

        binding.addNewLocationButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddLocationFragment()
            this.findNavController().navigate(action)
        }
    }

}