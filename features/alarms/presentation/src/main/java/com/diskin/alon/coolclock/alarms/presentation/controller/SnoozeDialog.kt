package com.diskin.alon.coolclock.alarms.presentation.controller

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

const val KEY_SNOOZE_VALUES = "snooze_values"
const val KEY_SNOOZE_ENTRIES = "snooze_entries"
const val KEY_CURRENT_SNOOZE = "current_snooze"

@AndroidEntryPoint
class SnoozeDialog : DialogFragment() {

    private lateinit var dialogListener: SnoozeDialogListener

    interface SnoozeDialogListener {
        fun onSnoozeDialogPositiveClick(selectedSnooze: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface
        try {
            dialogListener = parentFragment as SnoozeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((parentFragment.toString() +
                    " must implement SnoozeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val entries = requireArguments().getStringArray(KEY_SNOOZE_ENTRIES)!!
            val values = requireArguments().getIntArray(KEY_SNOOZE_VALUES)!!
            var selected = requireArguments().getInt(KEY_CURRENT_SNOOZE)
            MaterialAlertDialogBuilder(it)
                .setTitle("Alarm snooze")
                .setPositiveButton("ok") { _, _ ->
                    dialogListener.onSnoozeDialogPositiveClick(selected)
                }
                .setNegativeButton("cancel") { _, _ -> }
                .setSingleChoiceItems(entries,values.toList().indexOf(selected)){ dialog, which ->
                    selected = values[which]
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null for dialog creation")
    }
}