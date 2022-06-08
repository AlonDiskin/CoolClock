package com.diskin.alon.coolclock.worldclocks.presentation.controller

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.diskin.alon.coolclock.worldclocks.presentation.R

class CitiesTimeFragment : Fragment(R.layout.fragment_cities_time) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_cities_time, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_search -> {
                findNavController().navigate(R.id.action_worldClocksFragment_to_citiesSearchFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}