package com.diskin.alon.coolclock.alarms.presentation.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.alarms.presentation.databinding.AlarmBinding

class AlarmsAdapter(
    private val activationSwitchListener: (Int,Boolean) -> (Unit),
) : PagingDataAdapter<UiAlarm, AlarmsAdapter.AlarmViewHolder>(
    DIFF_CALLBACK
){

    companion object {

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UiAlarm>() {

            override fun areItemsTheSame(oldItem: UiAlarm, newItem: UiAlarm): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UiAlarm, newItem: UiAlarm) =
                oldItem == newItem
        }
    }

    class AlarmViewHolder(
        private val binding: AlarmBinding,
        private val activationSwitchListener: (Int,Boolean) -> (Unit)
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: UiAlarm) {
            binding.activeSwitcher.setOnCheckedChangeListener(null)
            binding.alarm = alarm
            binding.executePendingBindings()
            binding.activeSwitcher.setOnCheckedChangeListener { _, checked ->
                binding.alarm?.let { activationSwitchListener.invoke(it.id,checked) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return AlarmViewHolder(binding,activationSwitchListener)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}