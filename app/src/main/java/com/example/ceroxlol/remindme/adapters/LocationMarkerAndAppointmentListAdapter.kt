/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ceroxlol.remindme.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.databinding.ListAppointmentItemFragmentAppointmentListBinding
import com.example.ceroxlol.remindme.databinding.ListAppointmentsLocationHeaderBinding
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker

/**
 * [ListAdapter] implementation for the recyclerview.
 */

class LocationMarkerAndAppointmentListAdapter(
    private val onItemClicked: (AppointmentAndLocationMarker) -> Unit,
    private val onItemLongClicked: (AppointmentAndLocationMarker, View) -> Boolean
) :
    ListAdapter<AppointmentAndLocationMarker, RecyclerView.ViewHolder>(
        DiffCallback
    ) {

    // List<LocationMarkerAndAppointments>
    // LocationMarker : 0   -> appointment 0
    //                      -> appointment 2
    //                      -> appointment 4
    // LocationMarker : 1   -> appointment 0
    //                      -> appointment 1
    //                      -> appointment 8

    private val TYPE_LOCATION_MARKER_AND_APPOINTMENT = 0
    private val TYPE_APPOINTMENT = 1
    private var locationMarkerHeaderIndices: List<Int> = emptyList()

    override fun onCurrentListChanged(
        previousList: MutableList<AppointmentAndLocationMarker>,
        currentList: MutableList<AppointmentAndLocationMarker>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        locationMarkerHeaderIndices =
            currentList.mapIndexed { index, appointmentAndLocationMarker ->
                index to appointmentAndLocationMarker.locationMarker.id
            }.distinctBy {
                it.second
            }.map { it.first }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_APPOINTMENT) {
            return AppointmentViewHolder(
                ListAppointmentItemFragmentAppointmentListBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
            )
        } else if (viewType == TYPE_LOCATION_MARKER_AND_APPOINTMENT) {
            return LocationMarkerAndAppointmentViewHolder(
                ListAppointmentsLocationHeaderBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                )
            )
        }
        Log.e(LocationMarkerAndAppointmentListAdapter::class.java.simpleName, "Wrong viewType")
        throw RuntimeException("there is no type that matches the type $viewType make sure your using types correctly")
    }

    override fun getItemViewType(position: Int): Int {
        return if (position in locationMarkerHeaderIndices){
            TYPE_LOCATION_MARKER_AND_APPOINTMENT
        } else {
            TYPE_APPOINTMENT
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val current = getItem(position)
        //Appointment
        if (holder is AppointmentViewHolder) {
            holder.bind(current)
            holder.itemView.setOnClickListener {
                onItemClicked(current)
            }
            holder.itemView.setOnLongClickListener {
                onItemLongClicked(current, it)
            }
        }
        //Location and Appointment
        else if (holder is LocationMarkerAndAppointmentViewHolder) {
            holder.bind(current)
            holder.setOnClickListener(current)
            holder.setOnLongClickListener(current)
        }
    }

    inner class AppointmentViewHolder(private var binding: ListAppointmentItemFragmentAppointmentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(locationMarkerAndAppointments: AppointmentAndLocationMarker) {
            binding.appointmentName.text = locationMarkerAndAppointments.appointment.name
        }
    }

    inner class LocationMarkerAndAppointmentViewHolder(private var binding: ListAppointmentsLocationHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(locationMarkerAndAppointments: AppointmentAndLocationMarker) {
            binding.locationName.text = locationMarkerAndAppointments.locationMarker.name
            binding.appointmentName.text = locationMarkerAndAppointments.appointment.name
        }

        fun setOnClickListener(current: AppointmentAndLocationMarker) {
            binding.appointmentName.setOnClickListener{onItemClicked(current)}
        }

        fun setOnLongClickListener(current: AppointmentAndLocationMarker) {
            binding.appointmentName.setOnLongClickListener { onItemLongClicked(current, it) }
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AppointmentAndLocationMarker>() {
            override fun areItemsTheSame(
                oldItem: AppointmentAndLocationMarker,
                newItem: AppointmentAndLocationMarker
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: AppointmentAndLocationMarker,
                newItem: AppointmentAndLocationMarker
            ): Boolean {
                return oldItem.locationMarker.id == newItem.locationMarker.id
                        && oldItem.appointment.id == newItem.appointment.id
            }
        }
    }
}
