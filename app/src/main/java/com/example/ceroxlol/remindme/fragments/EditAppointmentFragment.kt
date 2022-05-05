package com.example.ceroxlol.remindme.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.ceroxlol.remindme.R
import com.example.ceroxlol.remindme.RemindMeApplication
import com.example.ceroxlol.remindme.adapters.ArrayAdapterAppointments
import com.example.ceroxlol.remindme.databinding.FragmentEditSingleAppointmentBinding
import com.example.ceroxlol.remindme.models.AppointmentKT
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModel
import com.example.ceroxlol.remindme.models.viewmodel.AppointmentKTViewModelFactory
import java.util.*

class EditAppointmentFragment : Fragment() {
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    lateinit var appointmentKT: AppointmentKT

    private val viewModel: AppointmentKTViewModel by activityViewModels {
        AppointmentKTViewModelFactory(
            (activity?.application as RemindMeApplication).database
                .appointmentDao()
        )
    }

    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentEditSingleAppointmentBinding? = null
    private val binding get() = _binding!!

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

    companion object {
        //TODO: Fix this!
        private lateinit var linearLayoutAppointments: LinearLayout
    }
}
