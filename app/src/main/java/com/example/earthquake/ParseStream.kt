package com.example.earthquake

import android.content.ContentValues
import android.location.Location
import android.util.JsonReader
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class ParseStream {
    @Throws(IOException::class)
    suspend fun parseXMLAsync(`in`: InputStream): List<Earthquake?> {
        val earthquakes: ArrayList<Earthquake?> = ArrayList(0)
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder = dbf.newDocumentBuilder()
        val dom: Document =
            withContext(Dispatchers.IO) {
                db.parse(`in`)
            }
        val docEle: Element = dom.getDocumentElement()
        val nl: NodeList = docEle.getElementsByTagName("entry")
        if (nl.getLength() > 0) {
            for (i in 0 until nl.getLength()) {
                val entry: Element = nl.item(i) as Element
                val id: Element =
                    entry.getElementsByTagName("id").item(0) as Element
                val title: Element =
                    entry.getElementsByTagName("title").item(0) as Element
                val g: Element = entry.getElementsByTagName("georss:point")
                    .item(0) as Element
                val `when`: Element =
                    entry.getElementsByTagName("updated").item(0) as Element
                val link: Element =
                    entry.getElementsByTagName("link").item(0) as Element
                val idString: String = id.getFirstChild().getNodeValue()
                var details: String = title.getFirstChild().getNodeValue()
                val hostname = "http://earthquake.usgs.gov"
                val linkString = hostname + link.getAttribute("href")
                val point: String = g.getFirstChild().getNodeValue()
                val dt: String = `when`.getFirstChild().getNodeValue()
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
                var qdate: Date = GregorianCalendar(0, 0, 0).getTime()
                try {
                    qdate = sdf.parse(dt)
                } catch (e: ParseException) {
                    Log.e(ContentValues.TAG, "Date parsing exception.", e)
                }
                val location =
                    point.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val l = Location("dummyGPS")
                l.setLatitude(location[0].toDouble())
                l.setLongitude(location[1].toDouble())
                val magnitudeString =
                    details.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                val end = magnitudeString.length - 1
                val magnitude = magnitudeString.substring(0, end).toDouble()
                details = if (details.contains("-")) details.split("-".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].trim { it <= ' ' } else ""
                val earthquake = Earthquake(
                    idString,
                    qdate,
                    details, l,
                    magnitude,
                    linkString
                )
                earthquakes.add(earthquake)
            }
        }
        return earthquakes
    }

    @Throws(IOException::class)
    fun parseXML(`in`: InputStream): List<Earthquake?> {
        val earthquakes: ArrayList<Earthquake?> = ArrayList(0)
        val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
        val db: DocumentBuilder = dbf.newDocumentBuilder()
        val dom: Document = db.parse(`in`)
        val docEle: Element = dom.getDocumentElement()
        val nl: NodeList = docEle.getElementsByTagName("entry")
        if (nl.getLength() > 0) {
            for (i in 0 until nl.getLength()) {
                val entry: Element = nl.item(i) as Element
                val id: Element =
                    entry.getElementsByTagName("id").item(0) as Element
                val title: Element =
                    entry.getElementsByTagName("title").item(0) as Element
                val g: Element = entry.getElementsByTagName("georss:point")
                    .item(0) as Element
                val `when`: Element =
                    entry.getElementsByTagName("updated").item(0) as Element
                val link: Element =
                    entry.getElementsByTagName("link").item(0) as Element
                val idString: String = id.getFirstChild().getNodeValue()
                var details: String = title.getFirstChild().getNodeValue()
                val hostname = "http://earthquake.usgs.gov"
                val linkString = hostname + link.getAttribute("href")
                val point: String = g.getFirstChild().getNodeValue()
                val dt: String = `when`.getFirstChild().getNodeValue()
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
                var qdate: Date = GregorianCalendar(0, 0, 0).getTime()
                try {
                    qdate = sdf.parse(dt)
                } catch (e: ParseException) {
                    Log.e(ContentValues.TAG, "Date parsing exception.", e)
                }
                val location =
                    point.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                val l = Location("dummyGPS")
                l.setLatitude(location[0].toDouble())
                l.setLongitude(location[1].toDouble())
                val magnitudeString =
                    details.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[1]
                val end = magnitudeString.length - 1
                val magnitude = magnitudeString.substring(0, end).toDouble()
                details = if (details.contains("-")) details.split("-".toRegex())
                    .dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1].trim { it <= ' ' } else ""
                val earthquake = Earthquake(
                    idString,
                    qdate,
                    details, l,
                    magnitude,
                    linkString
                )
                earthquakes.add(earthquake)
            }
        }
        return earthquakes
    }

    @Throws(IOException::class)
    suspend fun parseJsonAsync(`in`: InputStream): List<Earthquake?>? {
        var earthquakes: List<Earthquake?>? = null
        val reader = JsonReader(withContext(Dispatchers.IO) {
            InputStreamReader(`in`, "UTF-8")
        })
        withContext(Dispatchers.IO) {
            try {
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "features") {
                        earthquakes = readEarthquakeArray(reader)
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
            } finally {
                reader.close()
            }
        }
        return earthquakes
    }

    @Throws(IOException::class)
    fun parseJson(`in`: InputStream): List<Earthquake?>? {
        var earthquakes: List<Earthquake?>? = null
        val reader = JsonReader(InputStreamReader(`in`, "UTF-8"))
            try {
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    if (name == "features") {
                        earthquakes = readEarthquakeArray(reader)
                    } else {
                        reader.skipValue()
                    }
                }
                reader.endObject()
            } finally {
                reader.close()
            }
        return earthquakes
    }

    @Throws(IOException::class)
    fun readEarthquakeArray(reader: JsonReader): List<Earthquake?> {
        val earthquakes: MutableList<Earthquake?> = ArrayList()
        reader.beginArray()
        while (reader.hasNext()) {
            earthquakes.add(readEarthquake(reader))
        }
        reader.endArray()
        return earthquakes
    }

    @Throws(IOException::class)
    fun readEarthquake(reader: JsonReader): Earthquake {
        var id: String = ""
        var location: Location? = null
        var earthquakeProperties: Earthquake? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "id") {
                id = reader.nextString()
            } else if (name == "geometry") {
                location = readLocation(reader)
            } else if (name == "properties") {
                earthquakeProperties = readEarthquakeProperties(reader)
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return Earthquake(
            id,
            earthquakeProperties!!.getDate(),
            earthquakeProperties.getDetails(),
            location,
            earthquakeProperties.getMagnitude(),
            earthquakeProperties.getLink()
        )
    }

    @Throws(IOException::class)
    fun readEarthquakeProperties(reader: JsonReader): Earthquake {
        var date: Date? = null
        var details: String? = null
        var magnitude = -1.0
        var link: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "time") {
                val time = reader.nextLong()
                date = Date(time)
            } else if (name == "place") {
                details = reader.nextString()
            } else if (name == "url") {
                link = reader.nextString()
            } else if (name == "mag") {
                magnitude = reader.nextDouble()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return Earthquake("", date, details, null, magnitude, link)
    }

    @Throws(IOException::class)
    fun readLocation(reader: JsonReader): Location? {
        var location: Location? = null
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            if (name == "coordinates") {
                val coords = readDoublesArray(reader)
                location = Location("dummy")
                location.latitude = coords[0]
                location.longitude = coords[1]
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()
        return location
    }

    @Throws(IOException::class)
    fun readDoublesArray(reader: JsonReader): List<Double> {
        val doubles: MutableList<Double> = ArrayList()
        reader.beginArray()
        while (reader.hasNext()) {
            doubles.add(reader.nextDouble())
        }
        reader.endArray()
        return doubles
    }
}