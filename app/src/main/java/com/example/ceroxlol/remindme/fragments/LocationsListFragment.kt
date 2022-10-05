package com.example.ceroxlol.remindme.fragments

import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.LocationMarkerListAdapter
import com.example.ceroxlol.remindme.databinding.FragmentListLocationBinding
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentAndLocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentAndLocationMarkerViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LocationsListFragment : Fragment() {

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database.locationMarkerDao()
        )
    }

    private val appointmentAndLocationMarkerViewModel: AppointmentAndLocationMarkerViewModel by activityViewModels {
        AppointmentAndLocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }

    private var _binding: FragmentListLocationBinding? = null
    private val binding get() = _binding!!

    private var appointmentIdsUndoCache: HashMap<Int, List<Int>> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Beautify items
        val adapter = LocationMarkerListAdapter({
            val action = MainFragmentDirections.actionMainFragmentToEditLocationFragment(it.id)
            this.findNavController().navigate(action)
        }, { locationMarker, itemView ->
            handleLocationMarkerLongClick(itemView, locationMarker)
            true
        }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.

        locationMarkerViewModel.allLocations.observe(this.viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.addNewLocationButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddLocationFragment()
            this.findNavController().navigate(action)
        }
    }

    private fun handleLocationMarkerLongClick(
        itemView: View,
        locationMarker: LocationMarker
    ) {
        val transition = itemView.background as TransitionDrawable
        transition.startTransition(200)

        updateDeletionCache(locationMarker)

        deleteLocationMarker(locationMarker)

        createUndoSnackbar(itemView, locationMarker)
    }

    private fun updateDeletionCache(locationMarker: LocationMarker) {
        appointmentAndLocationMarkerViewModel.getAppointmentIdsByLocationMarkerId(locationMarker.id)
            .observe(this.viewLifecycleOwner) {
                appointmentIdsUndoCache[locationMarker.id] = it
            }
    }

    private fun deleteLocationMarker(locationMarker: LocationMarker) {
        appointmentAndLocationMarkerViewModel.deleteLocationMarkerForAppointments(locationMarker.id)
        locationMarkerViewModel.deleteLocationMarker(locationMarker)
    }

    private fun createUndoSnackbar(itemView: View, locationMarker: LocationMarker) {
        Snackbar.make(
            binding.recyclerView as View,
            "Undo delete ${locationMarker.name}",
            Snackbar.LENGTH_LONG
        )
            .setAction(
                "UNDO"
            ) {
                reconstructLocationMarkerForAppointment(locationMarker)
                val transition = itemView.background as TransitionDrawable
                transition.resetTransition()
                Toast.makeText(
                    requireContext(),
                    "${locationMarker.name} restored",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .show()
    }

    private fun reconstructLocationMarkerForAppointment(locationMarker: LocationMarker) {
        locationMarkerViewModel.addNewLocationMarker(locationMarker)
        appointmentIdsUndoCache[locationMarker.id]?.let {
            appointmentAndLocationMarkerViewModel.setLocationMarkerForAppointmentIds(locationMarker.id,
                it
            )
        }
    }
}