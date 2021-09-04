package me.aoyun.doodle

import android.graphics.Bitmap

object MemoryCache {

    fun getBitmap(key: Long): Bitmap? {
        var bitmap = LruCache.get(key);
        if (bitmap == null) {
            bitmap = WeakCache.get(key)
        }
        return bitmap
    }

    fun putBitmap(key: Long, bitmap: Bitmap, toWeakCache: Boolean) {
        if (toWeakCache) {
            WeakCache.put(key, bitmap)
        } else {
            LruCache.put(key, bitmap)
        }
    }
}