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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ceroxlol.remindme.databinding.ListAppointmentItemFragmentAppointmentListBinding
import com.example.ceroxlol.remindme.models.AppointmentAndLocationMarker

/**
 * [ListAdapter] implementation for the recyclerview.
 */

class LocationMarkerAndAppointmentListAdapter(
    private val onItemClicked: (AppointmentAndLocationMarker) -> Unit,
    private val onItemLongClicked: (AppointmentAndLocationMarker, View) -> Boolean
) :
    ListAdapter<AppointmentAndLocationMarker, LocationMarkerAndAppointmentListAdapter.AppointmentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        return AppointmentViewHolder(
            ListAppointmentItemFragmentAppointmentListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(current, it)
        }
        holder.bind(current)
    }

    inner class AppointmentViewHolder(private var binding: ListAppointmentItemFragmentAppointmentListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointmentAndLocationMarker: AppointmentAndLocationMarker) {
            binding.appointmentName.text = appointmentAndLocationMarker.appointments[0].text
            binding.appointmentLocationName.text = appointmentAndLocationMarker.locationMarker.name
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AppointmentAndLocationMarker>() {
            override fun areItemsTheSame(oldItem: AppointmentAndLocationMarker, newItem: AppointmentAndLocationMarker): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: AppointmentAndLocationMarker, newItem: AppointmentAndLocationMarker): Boolean {
                return oldItem.locationMarker.id == newItem.locationMarker.id
            }
        }
    }
}