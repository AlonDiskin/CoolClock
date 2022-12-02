package com.diskin.alon.coolclock.alarms.presentation.controller

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

const val KEY_DURATION_VALUES = "duration_values"
const val KEY_DURATION_ENTRIES = "duration_entries"
const val KEY_CURRENT_DURATION = "current_duration"

@AndroidEntryPoint
class DurationDialog : DialogFragment() {

    private lateinit var dialogListener: DurationDialogListener

    interface DurationDialogListener {
        fun onDurationDialogPositiveClick(selectedDuration: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface
        try {
            dialogListener = parentFragment as DurationDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((parentFragment.toString() +
                    " must implement DurationDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val entries = requireArguments().getStringArray(KEY_DURATION_ENTRIES)!!
            val values = requireArguments().getIntArray(KEY_DURATION_VALUES)!!
            var selected = requireArguments().getInt(KEY_CURRENT_DURATION)
            MaterialAlertDialogBuilder(it)
                .setTitle("Alarm duration")
                .setPositiveButton("ok") { _, _ ->
                    dialogListener.onDurationDialogPositiveClick(selected)
                }
                .setNegativeButton("cancel") { _, _ -> }
                .setSingleChoiceItems(entries,values.toList().indexOf(selected)){ dialog, which ->
                    selected = values[which]
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null for dialog creation")
    }
}