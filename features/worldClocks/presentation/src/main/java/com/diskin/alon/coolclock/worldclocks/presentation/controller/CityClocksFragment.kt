package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ShareCompat
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
import java.text.SimpleDateFormat
import java.util.*

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
            getString(com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,featureName),
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

                    R.id.action_share -> {
                        shareCityTime(cityClock)
                        true
                    }

                    else -> false
                }
            }
            inflate(R.menu.menu_city_clock)
            show()
        }
    }

    private fun shareCityTime(cityClock: UiCityClock) {
        activity?.let {
            ShareCompat.IntentBuilder
                .from(it)
                .setType(getString(R.string.mime_type_text))
                .setText(getString(R.string.share_city_time_message,cityClock.name,getCurrentCityTextTime(cityClock)))
                .setChooserTitle(getString(R.string.share_city_time_chooser_title))
                .startChooser()
        }
    }

    private fun getCurrentCityTextTime(cityClock: UiCityClock): String {
        val tz = TimeZone.getTimeZone(cityClock.gmt)
        val calendar = Calendar.getInstance(tz)
        val date = calendar.time
        val format = if(DateFormat.is24HourFormat(context)) {
            getString(R.string.clock_time_format_24)
        } else {
            getString(R.string.clock_time_format_12)
        }
        val df = SimpleDateFormat(format)
        df.timeZone = tz

        return df.format(date)
    }
}