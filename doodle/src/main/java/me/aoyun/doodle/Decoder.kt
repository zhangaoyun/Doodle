package me.aoyun.doodle

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 *
 */
object Decoder {

    const val NO_CLIP = -1
    private const val MATRIX = 0
    private const val CENTER = 5
    private const val CENTER_CROP = 6
    const val CENTER_INSIDE = 7

    fun decode(source: Source?, request: Request, fromDiskCache: Boolean): Bitmap? {
        if (source == null) {
            return null
        }

        var options = BitmapFactory.Options()
        options.inMutable = false
        options.inScaled = false
        options.inPreferredConfig = request.bitmapConfig

        val clipType = if (fromDiskCache) NO_CLIP else request.clipType

        var bitmap: Bitmap?

        // TODO 压缩 旋转 GIF
        bitmap = source.decode(options)

        return bitmap
    }
}