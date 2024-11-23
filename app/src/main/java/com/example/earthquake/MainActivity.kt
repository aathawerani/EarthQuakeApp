package com.example.earthquake

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity()/*, EarthquakeListFragment.OnListFragmentInteractionListener */{
    private val TAG_LIST_FRAGMENT = "TAG_LIST_FRAGMENT"
    var mEarthquakeListFragment: EarthquakeListFragment? = null
    private val eqViewModel: EarthquakeViewModel by viewModels()
    private val MENU_PREFERENCES: Int = Menu.FIRST + 1
    private val SHOW_PREFERENCES = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("Main", "Start")
        val fm: FragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            val ft: FragmentTransaction = fm.beginTransaction()
            mEarthquakeListFragment = EarthquakeListFragment()
            ft.add(
                R.id.main_activity_frame,
                mEarthquakeListFragment!!, TAG_LIST_FRAGMENT
            )
            ft.commitNow()
        } else {
            mEarthquakeListFragment = fm.findFragmentByTag(TAG_LIST_FRAGMENT) as EarthquakeListFragment?
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_settings)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.getItemId()) {
            MENU_PREFERENCES -> {
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivityForResult(intent, SHOW_PREFERENCES)
                return true
            }
        }
        return false
    }

    //override fun onListFragmentRefreshRequested() {
        //updateEarthquakes();
        //Log.d("onListFragmentRefreshRequested", "got here 203")
    //}

    //private fun updateEarthquakes() {
        //Log.d("updateEarthquakes", "Got here 1")
        // Request the View Model update the earthquakes from the USGS feed.
        //lifecycleScope.launch {
            //Log.d("updateEarthquakes", "Got here 2")
            //eqViewModel.loadEarthquakes()
            //Log.d("updateEarthquakes", "Got here 3")
        //}
    //}

}