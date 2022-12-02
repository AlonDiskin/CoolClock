package com.diskin.alon.coolclock.alarms.presentation.controller

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.databinding.FragmentAlarmEditorBinding
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmEditorViewModel
import com.diskin.alon.coolclock.common.presentation.VolumeButtonPressEvent
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AlarmEditorFragment : Fragment(),
    DurationDialog.DurationDialogListener, SnoozeDialog.SnoozeDialogListener,
    RingtoneDialog.RingtoneDialogListener {

    private val viewModel: AlarmEditorViewModel by viewModels()
    private lateinit var layout: FragmentAlarmEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        layout = DataBindingUtil.inflate(inflater,R.layout.fragment_alarm_editor,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Observe view model
        viewModel.alarmEdit.observe(viewLifecycleOwner) {
            layout.alarmEdit = it
            if (it.ringtone.isEmpty()) {
                layout.edit.volumeSeekBar.isEnabled = false
            }

            layout.edit.volumeSeekBar.min = it.minVolume
        }
        viewModel.scheduledAlarmDate.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                it,
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }
        viewModel.volumeButtonPress.observe(viewLifecycleOwner) {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                layout.edit.volumeSeekBar.tag = true
                when(it) {
                    VolumeButtonPressEvent.VOLUME_DOWN -> {
                        layout.edit.volumeSeekBar.progress = layout.edit.volumeSeekBar.progress - 1
                    }
                    VolumeButtonPressEvent.VOLUME_UP -> {
                        layout.edit.volumeSeekBar.progress = layout.edit.volumeSeekBar.progress + 1
                    }
                }
            }
        }

        // Set alarm time click listener
        layout.alarmTime.setOnClickListener(::onAlarmTimeClick)

        // Set ringtone edit click listener
        layout.edit.layoutSound.setOnClickListener(::onRingtoneSelectionClick)

        // Set vibration edit click listener
        layout.edit.layoutVibration.setOnClickListener(::onVibrationSelectionClick)

        // Set snooze edit click listener
        layout.edit.snooze.setOnClickListener(::onSnoozeSelectionClick)

        // Set duration edit click listener
        layout.edit.duration.setOnClickListener(::onDurationSelectionClick)

        // Set volume seekbar listener
        layout.edit.volumeSeekBar.tag = false
        layout.edit.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar, p1: Int, p2: Boolean) {
                if (layout.edit.volumeSeekBar.tag == true) {
                    layout.alarmEdit!!.volume = if (p1 == 0) 1 else p1
                    viewModel.alarmEdit.value?.let {
                        if (it.ringtone.isNotEmpty()) {
                            viewModel.playRingtoneSample(it.ringtone)
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                layout.edit.volumeSeekBar.tag = true
            }

            override fun onStopTrackingTouch(p0: SeekBar) {

            }
        })

        // Set floating action button click listener
        layout.fab.setOnClickListener(::onFabEditDoneClick)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopRingtonePlayback()
    }

    private fun onAlarmTimeClick(view: View) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(layout.alarmEdit!!.hour)
            .setMinute(layout.alarmEdit!!.minute)
            .setTitleText(getString(R.string.title_dialog_alarm_time_picker))
            .build()

        picker.addOnPositiveButtonClickListener {
            layout.alarmEdit?.let {  edit ->
                edit.hour = picker.hour
                edit.minute = picker.minute
                layout.alarmTime.text = edit.getTime()
            }
        }
        picker.show(childFragmentManager, "Alarm Material Time Picker")
    }

    private fun onRingtoneSelectionClick(view: View) {
        val dialog = RingtoneDialog()
        val bundle = Bundle()

        bundle.putStringArray(KEY_RINGTONE_VALUES,layout.alarmEdit!!.ringtoneValues)
        bundle.putStringArray(KEY_RINGTONE_ENTRIES,layout.alarmEdit!!.ringtoneEntries)
        bundle.putString(KEY_CURRENT_RINGTONE,layout.alarmEdit!!.ringtone)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "RingtoneDialogFragment")

        viewModel.stopRingtonePlayback()
    }

    private fun onVibrationSelectionClick(view: View) {
        layout.edit.switchVibration.performClick()
    }

    private fun onSnoozeSelectionClick(view: View) {
        val dialog = SnoozeDialog()
        val bundle = Bundle()

        bundle.putIntArray(KEY_SNOOZE_VALUES,layout.alarmEdit!!.snoozeValues.toIntArray())
        bundle.putStringArray(KEY_SNOOZE_ENTRIES,layout.alarmEdit!!.snoozeEntries)
        bundle.putInt(KEY_CURRENT_SNOOZE,layout.alarmEdit!!.snoozeValue)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "SnoozeDialogFragment")
    }

    private fun onDurationSelectionClick(view: View) {
        val dialog = DurationDialog()
        val bundle = Bundle()

        bundle.putIntArray(KEY_DURATION_VALUES,layout.alarmEdit!!.durationValues.toIntArray())
        bundle.putStringArray(KEY_DURATION_ENTRIES,layout.alarmEdit!!.durationEntries)
        bundle.putInt(KEY_CURRENT_DURATION,layout.alarmEdit!!.durationValue)
        dialog.arguments = bundle
        dialog.show(childFragmentManager, "DurationDialogFragment")
    }

    private fun onFabEditDoneClick(view: View) {
        view.isEnabled = false

        viewModel.schedule()
    }

    override fun onDurationDialogPositiveClick(selectedDuration: Int) {
        layout.alarmEdit!!.durationValue = selectedDuration
        layout.edit.durationValue.text = layout.alarmEdit!!.getDuration()
    }

    override fun onRingtoneDialogPositiveClick(ringtone: String) {
        layout.alarmEdit!!.ringtone = ringtone
        layout.edit.ringtone.text = layout.alarmEdit!!.getRingtoneName()
        layout.edit.volumeSeekBar.isEnabled = ringtone.isNotEmpty()
    }

    override fun onRingtoneSelection(path: String) {
        if (path.isNotEmpty()) {
            viewModel.playRingtoneSample(path)
        } else {
            viewModel.stopRingtonePlayback()
        }
    }

    override fun stopRingtoneSample() {
        viewModel.stopRingtonePlayback()
    }

    override fun onSnoozeDialogPositiveClick(selectedSnooze: Int) {
        layout.alarmEdit!!.snoozeValue = selectedSnooze
        layout.edit.stateSnooze.text = layout.alarmEdit!!.getSnooze()
    }
}