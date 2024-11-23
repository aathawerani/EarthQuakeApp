import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import androidx.work.*
import com.example.earthquake.*
import org.xml.sax.SAXException
import java.io.IOException
import java.net.MalformedURLException
import java.util.concurrent.TimeUnit
import javax.xml.parsers.ParserConfigurationException


class EarthquakeUpdateJobService(val context: Context, params: WorkerParameters) : Worker(context, params) {
    private val NOTIFICATION_CHANNEL = "earthquake"
    val NOTIFICATION_ID = 1

    override fun doWork(): Result {
        Log.d("EarthquakeUpdateJobService", "got here")
        try {
            val elist = EarthquakeListFragment()
            val earthquakes = elist.loadEarthquakes(context as Application)

                val largestNewEarthquake = findLargestNewEarthquake(earthquakes)
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val minimumMagnitude =
                    prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3")!!.toInt()
                if (largestNewEarthquake != null
                    && largestNewEarthquake.getMagnitude() >= minimumMagnitude
                ) {
                    broadcastNotification(largestNewEarthquake)
                }

            scheduleNextUpdate(context)
            return Result.success()
        } catch (e: MalformedURLException) {
            Log.e(ContentValues.TAG, "MalformedURLException", e)
            return Result.failure()
        } catch (e: IOException) {
            Log.e(ContentValues.TAG, "IOException", e)
            return Result.retry()
        } catch (e: ParserConfigurationException) {
            Log.e(ContentValues.TAG, "Parser Configuration Exception", e)
            return Result.failure()
        } catch (e: SAXException) {
            Log.e(ContentValues.TAG, "SAX Exception", e)
            return Result.failure()
        }
    }

    private fun scheduleNextUpdate(context: Context) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val updateFreq: Long =
            prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60")?.toLong() ?: 0
        val autoUpdateChecked: Boolean =
            prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false)
        if (autoUpdateChecked) {

            val constraints: Constraints = Constraints.Builder().apply {
                //setRequiredNetworkType(NetworkType.CONNECTED)
                //setRequiresCharging(true)
            }.build()

            val request: PeriodicWorkRequest =
                PeriodicWorkRequestBuilder<EarthquakeUpdateJobService>(updateFreq, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_JOB_TAG,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = (context as Application).resources.getString(
                com.example.earthquake.R.string.earthquake_channel_name)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.enableLights(true)
            val notificationManager: NotificationManager? = getSystemService(context,
                NotificationManager::class.java
            )
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun broadcastNotification(earthquake: Earthquake) {
        createNotificationChannel()
        val startActivityIntent = Intent(context,
            MainActivity::class.java
        )
        val launchIntent = PendingIntent.getActivity(
            context, 0,
            startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val earthquakeNotificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
        earthquakeNotificationBuilder
            .setSmallIcon(com.example.earthquake.R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(context, com.example.earthquake.R.color.teal_700))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(launchIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
        earthquakeNotificationBuilder
            .setWhen(earthquake.getDate()!!.time)
            .setContentTitle("M:" + earthquake.getMagnitude())
            .setContentText(earthquake.getDetails())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(earthquake.getDetails())
            )
        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(
            NOTIFICATION_ID,
            earthquakeNotificationBuilder.build()
        )
    }

    private fun findLargestNewEarthquake(
        newEarthquakes: ArrayList<Earthquake?>
    ): Earthquake? {
        val earthquakes = EarthquakeDatabaseAccessor
            .getInstance(applicationContext)
            .earthquakeDAO()
            ?.loadAllEarthquakesBlocking()
        var largestNewEarthquake: Earthquake? = null
        for (earthquake in newEarthquakes) {
            if (earthquakes!!.contains(earthquake)) {
                continue
            }
            if (earthquake != null) {
                if (largestNewEarthquake == null
                    || earthquake.getMagnitude() >
                    largestNewEarthquake.getMagnitude()
                ) {
                    largestNewEarthquake = earthquake
                }
            }
        }
        return largestNewEarthquake
    }


    override fun onStopped() {
        super.onStopped()
        TODO("Cleanup, because you are being stopped")
    }

    companion object {
        private val TAG = "EarthquakeUpdateJob"
        private val UPDATE_JOB_TAG = "update_job"
        private val PERIODIC_JOB_TAG = "periodic_job"

        fun scheduleUpdateJob(context: Context?) {
            Log.d("scheduleUpdateJob", "got here")
            val constraints: Constraints = Constraints.Builder().apply {
                //setRequiredNetworkType(NetworkType.CONNECTED)
                //setRequiresBatteryNotLow(true)
            }.build()

            val request: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<EarthquakeUpdateJobService>()
                    // Sets the input data for the ListenableWorker
                    //.setInputData(input)
                    // Other setters
                    .setConstraints(constraints)
                    .build()

            if (context != null) {
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(UPDATE_JOB_TAG,ExistingWorkPolicy.REPLACE,request)
            }

        }
    }
}
