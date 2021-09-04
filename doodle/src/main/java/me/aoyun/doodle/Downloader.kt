package me.aoyun.doodle

import android.util.Log
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit

object Downloader {

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)

        builder.build()
    }

    fun downloadOnly() {

    }

    fun getSource(request: okhttp3.Request): Any? {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val inputStream = response.body()?.byteStream()
            if (inputStream != null) {
                Log.d("AoyunTAG", "inputStream != null" )
            }
            return inputStream
        } else {
            throw IOException("request failed, " + response.code())
        }
    }
}