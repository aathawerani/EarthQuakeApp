package com.example.earthquake

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayList

class DBhelper {

    suspend fun DBInsertAsync(application: Application, earthquakes: ArrayList<Earthquake?>)
    {
        withContext(Dispatchers.IO) {
            EarthquakeDatabaseAccessor.getInstance(application)
                .earthquakeDAO()?.insertEarthquakes(earthquakes);
        }
    }

    fun DBInsert(application: Application, earthquakes: ArrayList<Earthquake?>)
    {
        EarthquakeDatabaseAccessor.getInstance(application)
            .earthquakeDAO()?.insertEarthquakes(earthquakes);
    }

    fun DBCount(application: Application) : Int?
    {
        return EarthquakeDatabaseAccessor.getInstance(application)
            .earthquakeDAO()?.getDataCount()
    }
}