package com.example.ceroxlol.remindme.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentAddAppointmentBinding
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.LocationMarker
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModelFactory

class AddNewAppointmentFragmentKT : Fragment() {

    private val viewModel: AppointmentKTViewModel by activityViewModels {
        AppointmentKTViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }
    private val navigationArgs: AddNewAppointmentFragmentKTArgs by navArgs()

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
            saveAction.setOnClickListener { updateItem() }
        }
    }

    private fun addNewAppointment() {
        if (isEntryValid()) {
            viewModel.addNewAppointmentKT(
                binding.appointmentName.text.toString(),
                binding.appointmentText.text.toString(),
                binding.appointmentLocation.selectedItem as LocationMarker,
                null,
                false
            )
            val action = AddNewAppointmentFragmentKTDirections.actionAddAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.appointmentName.text.toString(),
            binding.appointmentText.text.toString(),
            binding.appointmentLocation.selectedItem as LocationMarker
        )
    }

    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.appointmentId,
                this.binding.appointmentName.text.toString(),
                this.binding.appointmentText.text.toString(),
                this.binding.appointmentLocation.selectedItem as LocationMarker
            )
            val action = AddNewAppointmentFragmentKTDirections.actionAddAppointmentFragmentToMainFragment()
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.appointmentId
        if (id > 0) {
            viewModel.retrieveAppointmentKt(id).observe(this.viewLifecycleOwner) { selectedItem ->
                appointment = selectedItem
                bind(appointment)
            }
        } else {
            binding.saveAction.setOnClickListener {
                addNewAppointment()
            }
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