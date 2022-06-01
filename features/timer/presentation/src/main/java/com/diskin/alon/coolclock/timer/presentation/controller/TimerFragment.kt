package com.diskin.alon.coolclock.timer.presentation.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.diskin.alon.coolclock.timer.presentation.databinding.FragmentTimerBinding
import com.diskin.alon.coolclock.timer.presentation.model.UiTimerDuration
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

        // Set and config ui
        setLayout()

        // Set view model observers to handle model updates
        setViewModelObservers()
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

        // Save duration ui state
        viewModel.timerDuration.value = UiTimerDuration(
            layout.secondsPicker.value,
            layout.minutesPicker.value,
            layout.hoursPicker.value
        )
    }

    private fun setLayout() {
        setPickers()
        setInitialTimerLayout()
        setTimerButtonsListeners()
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

    private fun setInitialTimerLayout() {
        layout.motionLayout.setTransitionDuration(0)

        when(viewModel.timer.value?.state) {
            UiTimerState.NOT_SET,UiTimerState.DONE -> layout.motionLayout.transitionToStart()
            UiTimerState.PAUSED,UiTimerState.RESUMED,UiTimerState.RUNNING,UiTimerState.START ->
                layout.motionLayout.transitionToEnd()
        }

        layout.motionLayout.setTransitionDuration(400)
    }

    private fun setTimerButtonsListeners() {
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
    }

    private fun setViewModelObservers() {
        // Observe timer state
        viewModel.timer.observe(viewLifecycleOwner) {
            when(it.state) {
                UiTimerState.START -> {
                    layout.timer = it
                    if (layout.motionLayout.currentState == layout.motionLayout.startState) {
                        layout.motionLayout.transitionToEnd()
                    }
                }

                UiTimerState.RUNNING -> {
                    layout.timer = it
                    if (layout.buttonPauseResume.visibility == View.INVISIBLE) {
                        if (layout.motionLayout.currentState == layout.motionLayout.startState) {
                            layout.motionLayout.transitionToEnd()
                        }
                        layout.buttonStartCancel.text = "cancel"
                    }
                }

                UiTimerState.PAUSED -> {
                    layout.timer = it
                    layout.buttonPauseResume.text = "resume"
                    if (layout.buttonPauseResume.visibility == View.INVISIBLE) {
                        if (layout.motionLayout.currentState == layout.motionLayout.startState) {
                            layout.motionLayout.transitionToEnd()
                        }
                        layout.buttonStartCancel.text = "cancel"
                    }
                }

                UiTimerState.RESUMED -> layout.buttonPauseResume.text = "pause"

                UiTimerState.DONE -> {
                    layout.timer = it
                    if (layout.motionLayout.currentState == layout.motionLayout.endState) {
                        layout.motionLayout.transitionToStart()
                    }
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

        // Observe timer duration
        viewModel.timerDuration.observe(viewLifecycleOwner) {
            layout.secondsPicker.visibility = View.VISIBLE
            layout.minutesPicker.visibility = View.VISIBLE
            layout.hoursPicker.visibility = View.VISIBLE

            layout.secondsPicker.value = it.seconds
            layout.minutesPicker.value = it.minutes
            layout.hoursPicker.value = it.hours

            layout.buttonStartCancel.isEnabled = (layout.secondsPicker.value > 0) ||
                    (layout.minutesPicker.value > 0) || (layout.hoursPicker.value > 0)
        }
    }
}