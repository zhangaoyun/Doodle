package me.aoyun.doodle

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView

object Dispatcher {

    fun start(request: Request) {
        val bitmap = MemoryCache.getBitmap(request.key)

        var imageView: ImageView? = null
        if (request.targetReference != null) {
            imageView = request.targetReference!!.get()
        }

        if (bitmap == null) {
            Log.d("AoyunTAG", "开始 Work")
            val loader = Worker(request, imageView)
            loader.priority(request.priority)
                .hostHash(request.hostHash)
                .execute()
        }
    }

    fun feedback(request: Request, imageView: ImageView?, result: Any?, beforeLoading: Boolean) {
        if (imageView == null) {
            return
        }
        var bitmap = result as? Bitmap
        if (result != null) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }

    }
}