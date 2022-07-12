package com.diskin.alon.coolclock.worldclocks.presentation.controller

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
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.databinding.FragmentCityClocksBinding
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCityClock
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CityClocksViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class CityClocksFragment : Fragment(R.layout.fragment_city_clocks) {

    private val viewModel: CityClocksViewModel by viewModels()
    private lateinit var layout: FragmentCityClocksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentCityClocksBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set city clocks paging adapter
        val adapter = CityClocksAdapter(::handleClockMenuClick)
        layout.clocks.adapter = adapter

        adapter.addLoadStateListener(::handleLoadStateUpdate)

        // Observe view model state
        viewModel.cityClocks.observe(viewLifecycleOwner) { adapter.submitData(lifecycle,it) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_city_clocks_time, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_search -> {
                findNavController().navigate(R.id.action_cityClocksFragment_to_citiesSearchFragment)
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
            notifyUnknownError(getString(R.string.clocks_browser_feature))
        }
    }

    private fun notifyUnknownError(featureName: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.error_message_unknown,featureName),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun handleClockMenuClick(cityClock: UiCityClock, view: View) {
        PopupMenu(requireActivity(), view).apply {
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete_clock -> {
                        viewModel.deleteCityClock(cityClock)
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.menu_city_clock)
            show()
        }
    }
}