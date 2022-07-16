package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.coolclock.worldclocks.presentation.databinding.CityClockBinding
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock

class CityClocksAdapter(
    private val menuClickListener: (UiCityClock,View) -> (Unit)
) : PagingDataAdapter<UiCityClock, CityClocksAdapter.CityClockViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiCityClock>() {

            override fun areItemsTheSame(oldItem: UiCityClock, newItem: UiCityClock): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UiCityClock, newItem: UiCityClock) =
                oldItem == newItem
        }
    }

    class CityClockViewHolder(
        private val binding: CityClockBinding,
        private val menuClickListener: (UiCityClock,View) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.menuClickListener = menuClickListener
        }

        fun bind(cityClock: UiCityClock) {
            binding.cityClock = cityClock
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityClockViewHolder {
        val binding = CityClockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CityClockViewHolder(binding,menuClickListener)
    }

    override fun onBindViewHolder(holder: CityClockViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}