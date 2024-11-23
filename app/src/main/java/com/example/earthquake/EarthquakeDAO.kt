package com.example.earthquake

import android.content.Context
import androidx.room.*
import androidx.room.Room.databaseBuilder


@Dao
interface EarthquakeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEarthquakes(earthquakes: List<Earthquake?>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEarthquake(earthquake: Earthquake?)

    @Delete
    fun deleteEarthquake(earthquake: Earthquake?)

    @Query("SELECT * FROM earthquake ORDER BY mDate DESC")
    fun loadAllEarthquakes(): List<Earthquake>

    @Query("delete FROM earthquake")
    fun deleteAllEarthquakes()

    @Query("SELECT COUNT(mId) FROM earthquake")
    fun getDataCount(): Int

    @Query("SELECT * FROM earthquake ORDER BY mDate DESC")
    fun loadAllEarthquakesBlocking(): List<Earthquake?>?

}

@Database(entities = [Earthquake::class], version = 1)
@TypeConverters(*[EarthquakeTypeConverters::class])
abstract class EarthquakeDatabase : RoomDatabase() {
    abstract fun earthquakeDAO(): EarthquakeDAO?
}

class EarthquakeDatabaseAccessor {
    companion object {
        private var EarthquakeDatabaseInstance: EarthquakeDatabase? = null
        private val EARTHQUAKE_DB_NAME = "earthquake_db"
        fun getInstance(context: Context) : EarthquakeDatabase {
            if (EarthquakeDatabaseInstance == null) {
                EarthquakeDatabaseInstance = databaseBuilder(
                    context,
                    EarthquakeDatabase::class.java, EARTHQUAKE_DB_NAME
                ).build()
            }
            return EarthquakeDatabaseInstance as EarthquakeDatabase;
        }
    }
}
