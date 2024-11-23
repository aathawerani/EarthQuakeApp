package com.example.earthquake

import android.app.Application
import android.content.ContentValues.TAG
import android.location.Location
import android.util.JsonReader
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException


class EarthquakeViewModel (application: Application) : AndroidViewModel(application) {
    private var earthquakes: List<Earthquake>? = null
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance about to be destroyed")
    }

    suspend fun getEarthquakesAsync(): List<Earthquake>? {
        val dao = EarthquakeDatabaseAccessor.getInstance(getApplication()).earthquakeDAO()
        if(dao?.getDataCount() == 0){
            loadEarthquakesAsync()
        }
        if (earthquakes == null) {
            earthquakes = dao?.loadAllEarthquakes();
        }
        return earthquakes;
    }

    fun getEarthquakesFromDB(): List<Earthquake>? {
        val dao = EarthquakeDatabaseAccessor.getInstance(getApplication()).earthquakeDAO()
        if (earthquakes == null) {
            earthquakes = dao?.loadAllEarthquakes();
        }
        return earthquakes;
    }

    suspend fun loadEarthquakesAsync(): ArrayList<Earthquake?> {
        Log.d("loadEarthquakes", "Got here 1")
        val internet = InternetConnection()
        val parseStream = ParseStream()
        val quakeFeed: String =
            getApplication<Application>().resources.getString(R.string.earthquake_feed_json)

        var earthquakes: ArrayList<Earthquake?> = ArrayList(0)

        try {
            val `in` = internet.getStreamAsync(quakeFeed)
            if(`in` != null) {
                //earthquakes = parseXML(`in`) as ArrayList<Earthquake?>
                earthquakes =  parseStream.parseJsonAsync(`in`) as ArrayList<Earthquake?>

                val dbhelper = DBhelper()
                dbhelper.DBInsertAsync(getApplication(), earthquakes)
            }
            internet.disconnect()
        } catch (e: MalformedURLException) {
            Log.e(TAG, "MalformedURLException", e)
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
        } catch (e: ParserConfigurationException) {
            Log.e(TAG, "Parser Configuration Exception", e)
        } catch (e: SAXException) {
            Log.e(TAG, "SAX Exception", e)
        }
        Log.d("loadEarthquakes", "Got here 2")
        // Return our result array.
        return earthquakes
    }

    fun setEarthquakesJob() {
         EarthquakeUpdateJobService.scheduleUpdateJob(getApplication());
    }


    suspend fun deleteAll()
    {
        Log.d("deleteAll", "got here")
        val dao = EarthquakeDatabaseAccessor.getInstance(getApplication()).earthquakeDAO()
        if (dao != null) {
            withContext(Dispatchers.IO) {
                dao.deleteAllEarthquakes()
            }
        }
    }
}
