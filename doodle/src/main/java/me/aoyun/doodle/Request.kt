package me.aoyun.doodle

import android.graphics.Bitmap
import android.widget.ImageView
import com.horizon.doodle.worker.Priority
import java.lang.ref.WeakReference

class Request {

    val path: String
    val key: Long = MHash.hash64(toString())
    var priority = Priority.NORMAL
    var hostHash = 0
    var bitmapConfig = Bitmap.Config.ARGB_8888
    var clipType = Decoder.NO_CLIP

    var diskCacheStrategy = DiskCacheStrategy.ALL

    var cacheInterceptor: CacheInterceptor? = null

    var targetReference : WeakReference<ImageView>? = null
    var workerReference : WeakReference<Worker>? = null

    constructor(path: String) {
        this.path = path
    }

    fun into(target: ImageView) {
        targetReference = WeakReference(target)
        fillSizeAndLoad(target.width, target.height)
    }

    private fun fillSizeAndLoad(width: Int, height: Int) {
        Dispatcher.start(this)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        return stringBuilder.toString()
    }
}