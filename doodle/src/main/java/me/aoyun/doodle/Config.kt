package me.aoyun.doodle

import android.graphics.Bitmap

object Config {

    var memoryCacheCapacity = Runtime.getRuntime().maxMemory() / 6
    var diskCachePath = ""

    var diskCacheCapacity = 128L shl 20
    var sourceCacheCapacity: Long = 256L shl 20

    var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG

    var gifDecoder: GifDecoder? = null

    fun setGifDecoder(gifDecoder: GifDecoder): Config {
        this.gifDecoder = gifDecoder
        return this
    }
}