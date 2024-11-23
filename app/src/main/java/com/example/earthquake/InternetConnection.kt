package com.example.earthquake

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class InternetConnection {
    lateinit var httpConnection: HttpURLConnection

    suspend fun getStreamAsync(feed: String): InputStream? {
        val url: URL
        val quakeFeed: String = feed
        url = URL(quakeFeed)
        val connection: URLConnection
        connection = withContext(Dispatchers.IO) {
            url.openConnection()
        }
        httpConnection = connection as HttpURLConnection
        val responseCode: Int =
            withContext(Dispatchers.IO) {
                httpConnection.getResponseCode()
            }
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `in`: InputStream =
                withContext(Dispatchers.IO) {
                    httpConnection.getInputStream()
                }
            return `in`
        }
        return null
    }

    fun getStream(feed: String): InputStream? {
        val url: URL
        val quakeFeed: String = feed
        url = URL(quakeFeed)
        val connection: URLConnection
        connection = url.openConnection()
        httpConnection = connection as HttpURLConnection
        val responseCode: Int = httpConnection.getResponseCode()
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `in`: InputStream = httpConnection.getInputStream()
            return `in`
        }
        return null
    }

    fun disconnect()
    {
        httpConnection.disconnect()
    }
}