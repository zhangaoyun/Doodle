package me.aoyun.doodle

import android.graphics.Bitmap
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import com.horizon.doodle.worker.PipeExecutor
import com.horizon.doodle.worker.PriorityExecutor
import com.horizon.doodle.worker.TagExecutor
import com.horizon.doodle.worker.UITask
import java.io.File
import java.lang.ref.WeakReference

class Worker constructor(val request: Request, val imageView: ImageView?) :
    UITask<Void, Void, Any>() {

    init {
        if (imageView != null) {
            request.workerReference = WeakReference(this)
            imageView.tag = request
        }
    }

    var fromDiskCache = false

    private var key = request.key

    override val executor: PriorityExecutor
        get() = loadingExecutor

    private val target: ImageView?
        get() {
            if (request.targetReference != null) {
                val imageView = request.targetReference!!.get()
                if (imageView != null && imageView.tag == request) {
                    return imageView
                }
            }
            return null
        }

    companion object {
        private val cpuCount = Runtime.getRuntime().availableProcessors()
        private val windowSize = Math.max(4, Math.min(cpuCount + 1, 6))
        private val loadingExecutor = TagExecutor(PipeExecutor(windowSize))
        private val storageExecutor = PipeExecutor(1)

        fun storeResult(key: Long, bitmap: Bitmap) {
            storageExecutor.execute {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                DiskCache.put(key, bitmap)
            }
        }
    }

    override fun doInBackground(vararg params: Void): Any? {
        var bitmap: Bitmap? = null
        var source: Source? = null

        bitmap = MemoryCache.getBitmap(key)

        // 内存中没有缓存，尝试去磁盘中寻找
        if (bitmap == null) {
            // 图片的路径
            val filePath = DiskCache.get(key)
            fromDiskCache = !TextUtils.isEmpty(filePath)
            Log.d("AoyunTAG", "key: $key")
            Log.d("AoyunTAG", "fromDiskCache: $fromDiskCache")
            Log.d("AoyunTAG", "filePath: $filePath")
            source =
                if (fromDiskCache) Source.valueOf(File(filePath!!))
                else Source.parse(request)
            bitmap = Decoder.decode(source, request, fromDiskCache)

            // TODO transform

            // 得到了 bitmap
            if (bitmap != null) {
                // 处理内存缓存
                MemoryCache.checkMemory()
                if (request.memoryCacheStrategy != MemoryCacheStrategy.NONE) {
                    val toWeak = request.memoryCacheStrategy == MemoryCacheStrategy.WEAK
                    MemoryCache.putBitmap(key, bitmap, toWeak)
                }
                // 处理磁盘缓存
                if (!fromDiskCache && request.diskCacheStrategy != DiskCacheStrategy.NONE) {
                    storeResult(key, bitmap)
                }
            }

            Log.d("AoyunTAG", "doInBackground finish ${bitmap == null}")
            return bitmap
        }

        return null
    }

    override fun onPostExecute(result: Any?) {
        val imageView = target
        if (imageView != null) {

        }
        Dispatcher.feedback(request, imageView, result, false)
    }
}