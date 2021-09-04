package me.aoyun.doodle

import android.graphics.Bitmap
import android.os.Build
import java.io.Closeable

object Utils {

    fun getBytesCount(bitmap: Bitmap): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bitmap.allocationByteCount
        } else {
            bitmap.width * bitmap.height
        }
    }

    fun closeQuietly(closeable: Closeable?) {
        if (closeable != null) {
            closeable.close()
        }
    }
}