package com.example.ceroxlol.remindme.fragments

import android.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentAddAppointmentBinding
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModelFactory
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModel
import com.example.ceroxlol.remindme.models.viewmodel.LocationMarkerViewModelFactory


class AddNewAppointmentFragmentKT : Fragment() {

    private val appointmentViewModel: AppointmentKTViewModel by activityViewModels {
        AppointmentKTViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }

    private val locationMarkerViewModel: LocationMarkerViewModel by activityViewModels {
        LocationMarkerViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .locationMarkerDao()
        )
    }

    lateinit var appointment: AppointmentKT

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Binds views with the passed in item data.
     */
    private fun bind(appointmentKT: AppointmentKT) {
        binding.apply {
            appointmentName.setText(appointmentKT.name, TextView.BufferType.SPANNABLE)
            appointmentText.setText(appointmentKT.text, TextView.BufferType.SPANNABLE)
            //appointmentLocation.selectedItem(appointmentKT.location.id, TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { saveAppointment() }
        }
    }

    private fun saveAppointment() {
        if (isEntryValid()) {
            appointmentViewModel.addNewAppointmentKT(
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker,
                null,
                false
            )
            val action =
                AddNewAppointmentFragmentKTDirections.actionAddAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return appointmentViewModel.isEntryValid(
            binding.appointmentName.text.toString(),
            binding.appointmentText.text.toString(),
            binding.appointmentLocation.selectedItem as LocationMarker
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter : ArrayAdapter<LocationMarker> = ArrayAdapter(requireContext(), R.layout.simple_spinner_item)
        locationMarkerViewModel.allItems.observe(viewLifecycleOwner) {
            it?.forEach { locationMarker ->
                adapter.add(locationMarker)
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.appointmentLocation.adapter = adapter

        binding.saveAction.setOnClickListener {
            saveAppointment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }

}