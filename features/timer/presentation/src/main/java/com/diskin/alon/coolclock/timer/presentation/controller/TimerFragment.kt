package com.diskin.alon.coolclock.timer.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.diskin.alon.coolclock.timer.presentation.databinding.FragmentTimerBinding
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerState
import com.diskin.alon.coolclock.timer.presentation.viewmodel.TimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
@OptionalInject
class TimerFragment : Fragment() {

    private val viewModel: TimerViewModel by viewModels()
    private lateinit var layout: FragmentTimerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentTimerBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set number pickers
        setPickers()

        // Observe timer state
        viewModel.timer.observe(viewLifecycleOwner) {
            when(it.state) {
                UiTimerState.START -> {
                    layout.timer = it
                    layout.motionLayout.transitionToEnd()
                    layout.buttonStartCancel.text = "cancel"
                    layout.buttonStartCancel.isEnabled = true
                }
                UiTimerState.RUNNING -> {
                    layout.timer = it
                    layout.buttonStartCancel.isEnabled = true
                    if (layout.buttonPauseResume.visibility == View.INVISIBLE) {
                        layout.motionLayout.transitionToEnd()
                        layout.buttonStartCancel.text = "cancel"
                    }
                }
                UiTimerState.PAUSED -> {
                    layout.timer = it
                    layout.buttonStartCancel.isEnabled = true
                    layout.buttonPauseResume.text = "resume"
                    if (layout.buttonPauseResume.visibility == View.INVISIBLE) {
                        layout.motionLayout.transitionToEnd()
                        layout.buttonStartCancel.text = "cancel"
                    }
                }
                UiTimerState.RESUMED -> {
                    layout.buttonPauseResume.text = "pause"
                    layout.buttonStartCancel.isEnabled = true
                }
                UiTimerState.DONE -> {
                    layout.timer = it
                    layout.buttonStartCancel.text = "start"
                    layout.buttonPauseResume.text = "pause"
                    layout.motionLayout.transitionToStart()
                    layout.buttonStartCancel.isEnabled = (layout.secondsPicker.value > 0) ||
                            (layout.minutesPicker.value > 0) || (layout.hoursPicker.value > 0)
                }
            }
        }

        // Observe timer progress
        viewModel.progress.observe(viewLifecycleOwner) {
            if (it.max != layout.progressBar.max) {
                layout.progressBar.max = it.max
            }

            layout.progressBar.progress = it.progress
        }

        // Set start/cancel and pause buttons click listener
        layout.buttonStartCancel.setOnClickListener {
            when(viewModel.timer.value!!.state) {
                UiTimerState.NOT_SET, UiTimerState.DONE ->
                    viewModel.startTimer(getCurrentTimeFromTimerPickers())
                else -> viewModel.cancelTimer()
            }
        }
        layout.buttonPauseResume.setOnClickListener {
            when(viewModel.timer.value!!.state) {
                UiTimerState.PAUSED -> viewModel.resumeTimer()
                else -> viewModel.pauseTimer()
            }
        }

        // Set initial start\cancel button state
        layout.buttonStartCancel.isEnabled = (layout.secondsPicker.value > 0) ||
                (layout.minutesPicker.value > 0) || (layout.hoursPicker.value > 0)
    }

    override fun onStart() {
        super.onStart()
        // Hide status bar timer notification.if timer is running
        viewModel.hideTimerNotification()
    }

    override fun onStop() {
        super.onStop()
        // Show status bar timer notification.if timer is running
        viewModel.showTimerNotification()
    }

    private fun setPickers() {
        // Set range and current value
        layout.secondsPicker.minValue = 0
        layout.secondsPicker.maxValue = 59
        layout.secondsPicker.value = 0
        layout.minutesPicker.minValue = 0
        layout.minutesPicker.maxValue = 59
        layout.minutesPicker.value = 0
        layout.hoursPicker.maxValue = 99
        layout.hoursPicker.minValue = 0
        layout.hoursPicker.value = 0

        // Set 2 digit format
        val formatter: (Int) -> (String) = { String.format("%02d", it) }

        layout.secondsPicker.setFormatter(formatter)
        layout.minutesPicker.setFormatter(formatter)
        layout.hoursPicker.setFormatter(formatter)

        val pickersListener = NumberPicker.OnValueChangeListener { _, _, _ ->
            layout.buttonStartCancel.isEnabled = getCurrentTimeFromTimerPickers() > 0
        }

        layout.secondsPicker.setOnValueChangedListener(pickersListener)
        layout.hoursPicker.setOnValueChangedListener(pickersListener)
        layout.minutesPicker.setOnValueChangedListener(pickersListener)
    }

    private fun getCurrentTimeFromTimerPickers(): Long {
        val hours = layout.hoursPicker.value
        val minutes = layout.minutesPicker.value
        val seconds = layout.secondsPicker.value

        return TimeUnit.HOURS.toMillis(hours.toLong()) +
                TimeUnit.MINUTES.toMillis(minutes.toLong()) +
                TimeUnit.SECONDS.toMillis(seconds.toLong())
    }
}