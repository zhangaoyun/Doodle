package me.aoyun.doodle

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import com.horizon.doodle.worker.Priority
import java.lang.ref.WeakReference

class Request {

    private var sourceKey: String = ""
    val path: String
    var uri: Uri? = null

    /**
     * 注意 lazy，否则在 Request 类创建时就会给 key 赋值，导致所有 path 都是一样的
     */
    val key: Long by lazy { MHash.hash64(toString()) }
    var priority = Priority.NORMAL
    var hostHash = 0
    var bitmapConfig = Bitmap.Config.ARGB_8888
    var clipType = Decoder.NO_CLIP
    var placeholderResId: Int = -1
    var placeholderDrawable: Drawable? = null

    // decode parameter
    internal var viewWidth: Int = 0
    internal var viewHeight: Int = 0

    var memoryCacheStrategy = MemoryCacheStrategy.LRU
    var diskCacheStrategy = DiskCacheStrategy.ALL

    var cacheInterceptor: CacheInterceptor? = null

    var targetReference: WeakReference<ImageView>? = null
    var workerReference: WeakReference<Worker>? = null

    constructor(path: String) {
        Log.d("AoyunTAG", "load $path")
        if (TextUtils.isEmpty(path)) {
            this.path = ""
        } else {
            this.path =
                if (path.startsWith("http") || path.contains("://")) path
                else "file://$path"
        }
    }

    constructor(resId: Int) {
        path = Utils.toUriPath(resId)
        diskCacheStrategy = DiskCacheStrategy.NONE
    }

    fun into(target: ImageView) {
        targetReference = WeakReference(target)
        fillSizeAndLoad(target.width, target.height)
    }

    private fun fillSizeAndLoad(width: Int, height: Int) {
        Dispatcher.start(this)
    }

    fun placeholder(placeholderResId: Int): Request {
        this.placeholderResId = placeholderResId
        return this
    }

    override fun toString(): String {
        val builder = StringBuilder()
        if (!TextUtils.isEmpty(sourceKey)) {
            builder.append("source:").append(sourceKey)
        } else {
            builder.append("path:").append(path)
        }
        builder.append(" size:").append(viewWidth)
            .append('x').append(viewHeight)
            .append(" type:").append(clipType)
            .append(" config:").append(bitmapConfig)
//        if (!Utils.isEmpty(transformations)) {
//            builder.append(" transforms:")
//            for (transformation in transformations!!) {
//                builder.append(' ').append(transformation.key())
//            }
//        }
        return builder.toString()
    }
}