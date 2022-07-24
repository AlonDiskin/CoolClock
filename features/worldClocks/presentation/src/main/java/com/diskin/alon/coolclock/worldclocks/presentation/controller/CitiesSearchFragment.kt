package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.diskin.alon.coolclock.worldclocks.application.util.AppError
import com.diskin.alon.coolclock.worldclocks.presentation.R
import com.diskin.alon.coolclock.worldclocks.presentation.databinding.FragmentCitiesSearchBinding
import com.diskin.alon.coolclock.worldclocks.presentation.model.UiCitySearchResult
import com.diskin.alon.coolclock.worldclocks.presentation.viewmodel.CitiesSearchViewModel
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.migration.OptionalInject

@AndroidEntryPoint
@OptionalInject
class CitiesSearchFragment : Fragment(), MenuItem.OnActionExpandListener, SearchView.OnQueryTextListener {

    private val viewModel: CitiesSearchViewModel by viewModels()
    private lateinit var layout: FragmentCitiesSearchBinding
    private var collapsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        layout = FragmentCitiesSearchBinding.inflate(inflater,container,false)
        return layout.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_cities_search,menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        val editText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)

        editText.setHintTextColor(Color.LTGRAY)
        editText.setTextColor(
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorOnPrimary,
                Color.BLACK
            )
        )
        searchView.queryHint = getString(R.string.search_hint)

        searchItem.expandActionView()
        searchView.setQuery(viewModel.searchText,false)
        searchView.setOnQueryTextListener(this)
        searchItem.setOnActionExpandListener(this)
    }

    override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
        collapsed = false
        return true
    }

    override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
        findNavController().navigateUp()
        collapsed = true
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { viewModel.search(it) }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.let {
            if (isVisible) {
                viewModel.searchText = it
            }
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set search results paging adapter
        val adapter = CitiesSearchResultsAdapter(::handleResultAddClick)
        layout.results.adapter = adapter

        adapter.addLoadStateListener(::handleLoadStateUpdate)

        // Observe view model state
        viewModel.results.observe(viewLifecycleOwner) { adapter.submitData(lifecycle,it) }
        viewModel.addedCity.observe(viewLifecycleOwner,::notifyCityAddedToUserClocks)
        viewModel.addCityError.observe(viewLifecycleOwner,::handleAddCityError)
    }

    @VisibleForTesting
    fun handleLoadStateUpdate(state: CombinedLoadStates) {
        // Handle loading ui indicator
        layout.progressBar.isVisible = state.refresh is LoadState.Loading ||
                state.append is LoadState.Loading

        // Handle error
        if (state.append is LoadState.Error || state.refresh is LoadState.Error) {
            notifyUnknownError("Search")
        }
    }

    private fun notifyUnknownError(featureName: String) {
        Toast.makeText(
            requireContext(),
            getString(com.diskin.alon.coolclock.common.presentation.R.string.error_message_unknown,featureName),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun handleResultAddClick(result: UiCitySearchResult) {
        viewModel.addCity(result)
    }

    private fun notifyCityAddedToUserClocks(name: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.city_added_message,name),
            Toast.LENGTH_LONG)
            .show()
    }

    private fun handleAddCityError(error: AppError) {
        when(error) {
            AppError.UNKNOWN_ERROR -> notifyUnknownError("Add city")
        }
    }
}