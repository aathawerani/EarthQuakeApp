package com.example.earthquake

import android.location.Location
import androidx.room.TypeConverter
import java.util.*

class EarthquakeTypeConverters {
    @TypeConverter
    fun dateFromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }

    @TypeConverter
    fun locationToString(location: Location?): String? {
        return if (location == null) null else location.getLatitude().toString() + "," +
                location.getLongitude()
    }

    @TypeConverter
    fun locationFromString(location: String?): Location? {
        return if (location != null && location.contains(",")) {
            val result = Location("Generated")
            val locationStrings = location.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (locationStrings.size == 2) {
                result.setLatitude(locationStrings[0].toDouble())
                result.setLongitude(locationStrings[1].toDouble())
                result
            } else null
        } else null
    }
}
