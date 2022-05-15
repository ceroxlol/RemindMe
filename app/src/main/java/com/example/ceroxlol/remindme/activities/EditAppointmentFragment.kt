/*
package com.example.ceroxlol.remindme.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.databinding.FragmentEditSingleAppointmentBinding
import com.example.ceroxlol.remindme.fragments.EditAppointmentFragmentArgs
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModelFactory
import java.util.*

class EditAppointmentFragment : Fragment() {

    private val viewModel: AppointmentKTViewModel by activityViewModels {
        AppointmentKTViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }
    private val navigationArgs: EditAppointmentFragmentArgs by navArgs()

    lateinit var appointment: AppointmentKT

    private var _binding: FragmentEditSingleAppointmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSingleAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_appointment)


        Log.i("EditAppointmentActivity", "Initializing edit appointments activity.")
        linearLayoutAppointments = findViewById(R.id.linearLayoutEditAppointmentsAppointments)
        appointmentArrayList = getDb().appointmentDao().getAll() as ArrayList<AppointmentKT>
        val mAppointmentsAdapter =
            ArrayAdapterAppointments(this, appointmentArrayList, linearLayoutAppointments)

        Log.i("EditAppointmentActivity", "Found " + mAppointmentsAdapter.count + " appointments.")
        for (i in 0 until mAppointmentsAdapter.count) {
            val view: View = mAppointmentsAdapter.getView(i, null, linearLayoutAppointments)
            linearLayoutAppointments.addView(view)
        }
    }


}
*/
