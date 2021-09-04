package me.aoyun.doodle

import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

object Downloader {

    private val client: OkHttpClient by lazy {
        val cacheDirPath = Utils.cacheDir + "/doodle/source/"
        val sourceCacheCapacity = Config.sourceCacheCapacity
        val maxSize = if (sourceCacheCapacity > 0) sourceCacheCapacity else 256L shl 20
        val builder = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .cache(Cache(File(cacheDirPath), maxSize))

        builder.build()
    }

    fun downloadOnly() {

    }

    fun getSource(request: okhttp3.Request): Any? {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val inputStream = response.body()?.byteStream()
            if (inputStream != null) {
                Log.d("AoyunTAG", "inputStream != null")
            }
            return inputStream
        } else {
            throw IOException("request failed, " + response.code())
        }
    }
}