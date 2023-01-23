package com.diskin.alon.coolclock.alarms.presentation.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val KEY_RINGTONE_VALUES = "ringtone_values"
const val KEY_RINGTONE_ENTRIES = "ringtone_entries"
const val KEY_CURRENT_RINGTONE = "ringtone_duration"

class RingtoneDialog : DialogFragment() {

    private lateinit var dialogListener: RingtoneDialogListener

    interface RingtoneDialogListener {

        fun onRingtoneDialogPositiveClick(ringtone: String)

        fun onRingtoneSelection(path: String)

        fun stopRingtoneSample()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host fragment implements the callback interface
        try {
            dialogListener = parentFragment as RingtoneDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((parentFragment!!.javaClass.name.toString() +
                    " must implement RingtoneDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val entries = requireArguments().getStringArray(KEY_RINGTONE_ENTRIES)!!
            val values = requireArguments().getStringArray(KEY_RINGTONE_VALUES)!!
            var selected = requireArguments().getString(KEY_CURRENT_RINGTONE)!!
            MaterialAlertDialogBuilder(it)
                .setTitle("Alarm ringtone")
                .setPositiveButton("ok") { _, _ ->
                    dialogListener.onRingtoneDialogPositiveClick(selected)
                }
                .setNegativeButton("cancel") { _, _ -> }
                .setSingleChoiceItems(entries,values.toList().indexOf(selected)){ dialog, which ->
                    selected = values[which]
                    dialogListener.onRingtoneSelection(selected)
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null for dialog creation")
    }

    override fun onDetach() {
        super.onDetach()
        dialogListener.stopRingtoneSample()
    }
}