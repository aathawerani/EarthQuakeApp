package com.example.earthquake

import EarthquakeUpdateJobService
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EarthquakeListFragment : Fragment() {
    private var mRecyclerView: RecyclerView? = null
    private var mSwipeToRefreshView: SwipeRefreshLayout? = null
    private val eqViewModel: EarthquakeViewModel by viewModels()
    private var job: Job? = null
    //private var mListener: OnListFragmentInteractionListener? = null
    private var mMinimumMagnitude = 0
    private var refreshtime = 0
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_earthquake_list, container, false)
        mSwipeToRefreshView = view.findViewById(R.id.swiperefresh);
        mRecyclerView = view.findViewById<View>(R.id.list) as RecyclerView?

        //handler.postDelayed(m_Runnable, refreshtime.toLong());

        return view
    }

    private val m_Runnable: Runnable = object : Runnable {
        override fun run() {
            Log.d("run", "got here")
            viewLifecycleOwner.lifecycleScope.launch {
                setEarthquakesAsync()
            }
            handler.postDelayed(this, refreshtime.toLong())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the Recycler View adapter
        val context: Context = view.context
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //setEarthquakesAsync()
                setEarthquakesDB()
            }
        }
        mSwipeToRefreshView!!.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                //setEarthquakesAsync()
                setEarthquakesDB()
            }
            mSwipeToRefreshView!!.isRefreshing = false
        }
    }

    private fun updateFromPreferences() {
        val prefs: SharedPreferences? = getContext()?.let {
            PreferenceManager.getDefaultSharedPreferences(
                it
            )
        }
        if (prefs != null) {
            mMinimumMagnitude = prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3")!!.toInt()
            refreshtime = prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "1")!!.toInt()
            refreshtime = refreshtime * 1000
        }
    }

    protected suspend fun setEarthquakesAsync() {
        var earthquakes: ArrayList<Earthquake>
        withContext(Dispatchers.IO) {
            earthquakes = (eqViewModel.getEarthquakesAsync() as ArrayList<Earthquake>?)!!
            updateFromPreferences()
            if (earthquakes.size > 0) {
                for (i in earthquakes.size - 1 downTo 0) {
                    if (earthquakes.get(i).getMagnitude() < mMinimumMagnitude) {
                        earthquakes.removeAt(i)
                    }
                }
            }
        }
        mRecyclerView?.adapter = EarthquakeRecyclerViewAdapter(earthquakes)
    }

    protected suspend fun setEarthquakesDB() {
        var earthquakes: ArrayList<Earthquake>
        withContext(Dispatchers.IO) {
            earthquakes = (eqViewModel.getEarthquakesFromDB() as ArrayList<Earthquake>?)!!
            updateFromPreferences()
            if (earthquakes.size > 0) {
                for (i in earthquakes.size - 1 downTo 0) {
                    if (earthquakes.get(i).getMagnitude() < mMinimumMagnitude) {
                        earthquakes.removeAt(i)
                    }
                }
            }
        }
        mRecyclerView?.adapter = EarthquakeRecyclerViewAdapter(earthquakes)
    }

    fun setEarthquakesJob() {
            eqViewModel.setEarthquakesJob()
    }

    fun loadEarthquakes(application: Application): ArrayList<Earthquake?> {
        val internet = InternetConnection()
        val parseStream = ParseStream()
        val quakeFeed: String = application.resources.getString(R.string.earthquake_feed_json)
        var earthquakes: java.util.ArrayList<Earthquake?> = java.util.ArrayList(0)

        val `in` = internet.getStream(quakeFeed)
        if (`in` != null) {
            //earthquakes = parseXML(`in`) as ArrayList<Earthquake?>
            earthquakes = parseStream.parseJson(`in`) as java.util.ArrayList<Earthquake?>

            val dbhelper = DBhelper()
            //dbhelper.DBInsert(application, earthquakes)
            if(dbhelper.DBCount(application)!! > 0)
            {
                Log.d("loadEarthquakes", "DB populated")
            }
            else
            {
                Log.d("loadEarthquakes", "DB not populated")
            }
        }
        internet.disconnect()
        return earthquakes
    }


    override fun onStart() {
        super.onStart()
        //job = viewLifecycleOwner.lifecycleScope.launch {
            //val earthquakes = eqViewModel.loadEarthquakes()
            //mRecyclerView?.adapter = EarthquakeRecyclerViewAdapter(earthquakes)
            //setEarthquakesAsync()
            //setEarthquakesJob()
        //}
        setEarthquakesJob()
    }
    override fun onStop() {
        super.onStop()
        job?.cancel()
        viewLifecycleOwner.lifecycleScope.launch {
            eqViewModel.deleteAll()
        }
        //handler.removeCallbacks(m_Runnable)
    }

    //override fun onAttach(context: Context) {
        //super.onAttach(context)
        //mListener = context as OnListFragmentInteractionListener
        //Log.d("onAttach", "got here 201")
    //}

    //override fun onDetach() {
        //super.onDetach()
        //mListener = null
        //Log.d("onDetach", "got here 202")
    //}

    //interface OnListFragmentInteractionListener {
        //fun onListFragmentRefreshRequested()
    //}

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val mPrefListener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (PreferencesActivity.PREF_MIN_MAG == key) {
                viewLifecycleOwner.lifecycleScope.launch {
                    //setEarthquakesAsync()
                    setEarthquakesDB()
                }
            }
        }

        val prefs = getContext()?.let { PreferenceManager.getDefaultSharedPreferences(it) };
        if (prefs != null) {
            prefs.registerOnSharedPreferenceChangeListener(mPrefListener)
        };

    }
}


