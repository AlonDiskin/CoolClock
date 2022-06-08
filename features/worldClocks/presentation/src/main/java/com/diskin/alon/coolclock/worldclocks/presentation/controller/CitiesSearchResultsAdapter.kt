package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.coolclock.worldclocks.presentation.databinding.CitySearchResultBinding
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult

class CitiesSearchResultsAdapter : PagingDataAdapter<UiCitySearchResult, CitiesSearchResultsAdapter.ResultViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiCitySearchResult>() {

            override fun areItemsTheSame(oldItem: UiCitySearchResult, newItem: UiCitySearchResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UiCitySearchResult, newItem: UiCitySearchResult) =
                oldItem == newItem
        }
    }

    class ResultViewHolder(
        private val binding: CitySearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: UiCitySearchResult) {
            binding.result = result
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = CitySearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}