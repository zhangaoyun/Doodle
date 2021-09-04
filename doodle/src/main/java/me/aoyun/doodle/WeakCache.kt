package me.aoyun.doodle

import android.graphics.Bitmap
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

object WeakCache {

    private val cache = HashMap<Long, BitmapWeakReference>()
    private val queue = ReferenceQueue<Bitmap>()

    class BitmapWeakReference constructor(
        val key: Long,
        val bitmap: Bitmap,
        val queue: ReferenceQueue<Bitmap>
    ) : WeakReference<Bitmap>(bitmap, queue)

    @Synchronized
    fun get(key: Long): Bitmap? {
        cleanQueue()
        val bitmapWeakReference = cache[key]
        if (bitmapWeakReference != null) {
            val bitmap = bitmapWeakReference.get()
            if (bitmap != null) {
                return bitmap
            }
        }
        return null
        // return cache[key]?.get()
    }

    @Synchronized
    fun put(key: Long, bitmap: Bitmap) {
        cleanQueue()
        var bitmapWeakReference = cache[key]
        if (bitmapWeakReference == null) {
            bitmapWeakReference = BitmapWeakReference(key, bitmap, queue)
        }
    }

    private fun cleanQueue() {
        var bitmapWeakReference = queue.poll() as BitmapWeakReference?
        while (bitmapWeakReference != null) {
            cache.remove(bitmapWeakReference.key)
            bitmapWeakReference = queue.poll() as BitmapWeakReference?
        }
    }
}