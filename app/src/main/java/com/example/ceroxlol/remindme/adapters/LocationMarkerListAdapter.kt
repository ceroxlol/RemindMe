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
import com.example.ceroxlol.remindme.databinding.LocationListItemBinding
import com.example.ceroxlol.remindme.models.LocationMarker

/**
 * [ListAdapter] implementation for the recyclerview.
 */

class LocationMarkerListAdapter(
    private val onItemClicked: (LocationMarker) -> Unit,
    private val onItemLongClicked: (LocationMarker, View) -> Boolean
) :
    ListAdapter<LocationMarker, LocationMarkerListAdapter.LocationMarkerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationMarkerViewHolder {
        return LocationMarkerViewHolder(
            LocationListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationMarkerViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.itemView.setOnLongClickListener {
            onItemLongClicked(current, it)
        }
        holder.bind(current)
    }

    class LocationMarkerViewHolder(private var binding: LocationListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(locationMarker: LocationMarker) {
            binding.locationName.text = locationMarker.name
            binding.locationAddress.text = "No Address"
            binding.locationDistance.text = "0 KM"
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<LocationMarker>() {
            override fun areItemsTheSame(
                oldItem: LocationMarker,
                newItem: LocationMarker
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: LocationMarker,
                newItem: LocationMarker
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
