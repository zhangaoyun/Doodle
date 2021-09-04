package me.aoyun.doodle

import android.content.ComponentCallbacks2
import android.graphics.Bitmap

object LruCache {

    /**
     * 第三个参数：accessOrder，传入 true 时， 元素会按访问顺序排列，最后访问的在遍历器最后端。
     */
    private val cache = LinkedHashMap<Long, BitmapWrapper>(16, 0.75f, true)
    private var size: Long = 0
    private val minMemory = Runtime.getRuntime().maxMemory() / 32

    @Synchronized
    fun get(key: Long): Bitmap? {
        return cache[key]?.bitmap
    }

    @Synchronized
    fun put(key: Long, bitmap: Bitmap) {
        val capacity = Config.memoryCacheCapacity

        var bitmapWrapper = cache[key]
        if (bitmapWrapper == null) {
            bitmapWrapper = BitmapWrapper(bitmap, Utils.getBytesCount(bitmap))
            size += Utils.getBytesCount(bitmap)
            if (size > capacity) {
                trimToSize(capacity * 9 / 10)
            }
        }
    }

    @Synchronized
    fun clearMemory() {
        trimToSize(0)
    }

    @Synchronized
    fun trimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            trimToSize(0)
        } else if (level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            trimToSize(Math.max(size shr 1, minMemory))
        }
    }

    private fun trimToSize(expectedSize: Long) {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext() && size > expectedSize) {
            val entry = iterator.next()
            val bitmapWrapper = entry.value
            WeakCache.put(entry.key, bitmapWrapper.bitmap)
            iterator.remove()
            size -= bitmapWrapper.bytesCount
        }
    }


}