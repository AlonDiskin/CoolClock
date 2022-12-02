package com.diskin.alon.coolclock.alarms.presentation.controller

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.coolclock.alarms.presentation.R
import com.diskin.alon.coolclock.alarms.presentation.databinding.FragmentAlarmsBinding
import com.diskin.alon.coolclock.alarms.presentation.model.UiAlarm
import com.diskin.alon.coolclock.alarms.presentation.viewmodel.AlarmsViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class AlarmsFragment : Fragment() {

    private val viewModel: AlarmsViewModel by viewModels()
    private lateinit var layout: FragmentAlarmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentAlarmsBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set alarms paging adapter
        val adapter = AlarmsAdapter(::handleAlarmActivationSwitch,::handleAlarmMenuClick)
        layout.alarms.adapter = adapter

        adapter.addLoadStateListener(::handleLoadStateUpdate)

        // Observe view model state
        viewModel.alarms.observe(viewLifecycleOwner) { adapter.submitData(lifecycle,it) }
        viewModel.latestScheduledAlarm.observe(viewLifecycleOwner,::notifyOfLatestScheduledAlarm)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_alarms, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_add_alarm -> {
                findNavController().navigate(R.id.action_alarmsFragment_to_alarmEditorFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @VisibleForTesting
    fun handleLoadStateUpdate(state: CombinedLoadStates) {
        // Handle loading ui indicator
        layout.progressBar.isVisible = state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading

        // Handle error
        if (state.append is LoadState.Error || state.refresh is LoadState.Error) {
            notifyUnknownError(getString(R.string.alarms_browser_feature))
        }
    }

    private fun notifyUnknownError(featureName: String) {
        Toast.makeText(
            requireContext(),
            getString(com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,featureName),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun handleAlarmActivationSwitch(id: Int,activation: Boolean) {
        viewModel.changeAlarmActivation(id,activation)
    }

    private fun notifyOfLatestScheduledAlarm(date: String) {
        Toast.makeText(
            requireContext(),
            date,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun handleAlarmMenuClick(alarm: UiAlarm,view: View) {
        PopupMenu(requireActivity(), view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete_alarm -> {
                        viewModel.deleteAlarm(alarm.id)
                        true
                    }

                    R.id.action_edit_alarm -> {
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.menu_alarm)
            show()
        }
    }
}