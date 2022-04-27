package com.diskin.alon.coolclock.home.presentation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.diskin.alon.coolclock.home.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var graphProvider: AppGraphProvider
    private lateinit var layout: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set layout binding
        layout = DataBindingUtil.setContentView(this,R.layout.activity_main)

        // Set toolbar
        setSupportActionBar(layout.toolbar)

        // Set navigation
        val navController = if (savedInstanceState == null) {
            val host = NavHostFragment.create(graphProvider.getAppGraph())
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, host)
                .setPrimaryNavigationFragment(host)
                .commitNow()

            host.navController
        } else {
            (supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment)
                .navController
        }

        layout.bottomNav.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val controller = findNavController(R.id.nav_host_container)
                val request = NavDeepLinkRequest.Builder
                    .fromUri(getString(com.diskin.alon.coolclock.common.presentation.R.string.uri_settings).toUri())
                    .build()

                controller.navigate(request)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Do not return to start dest upon back from sub graphs root dest
        val controller = findNavController(R.id.nav_host_container)
        if (controller.currentDestination!!.id == graphProvider.getTimerDest() ||
            controller.currentDestination!!.id == graphProvider.getClocksDest()
        ) {
            finish()
            return
        }
        super.onBackPressed()
    }
}