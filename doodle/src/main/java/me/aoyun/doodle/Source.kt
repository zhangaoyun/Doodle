package me.aoyun.doodle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import okhttp3.CacheControl
import java.io.*

abstract class Source : Closeable {

    // 魔数，提供文件格式的信息
    abstract val magic: Int
    var magicNum: Int = 0

    // 旋转方向，EXIF专属信息
    abstract val orientation: Int

    abstract fun decode(options: BitmapFactory.Options): Bitmap?

    abstract fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap?

    companion object {
        fun valueOf(src: Any?): Source {
            if (src == null) {
                throw IllegalArgumentException("source is null")
            }
            return when (src) {
                is File -> FileSource(src)
                is InputStream -> StreamSource(src)
                else -> throw IllegalArgumentException("Unsupported src " + src.javaClass.simpleName)
            }
        }

        fun parse(request: Request): Source? {
            val path = request.path
            return when {
                path.startsWith("http") -> {
                    Log.d("AoyunTAG", "path: $path")
                    checkCache(request) ?: let {
                        val builder = okhttp3.Request.Builder().url(path)
                        if (request.diskCacheStrategy == DiskCacheStrategy.NONE) {
                            builder.cacheControl(CacheControl.Builder().noCache().noCache().build())
                        }
                        valueOf(Downloader.getSource(builder.build()))
                    }
                }
                // TODO
                else -> null
            }
        }

        private fun checkCache(request: Request): Source? {
            val url = request.path
            if (request.cacheInterceptor != null) {

            }
            return null
        }
    }

}

class FileSource(private val file: File) : Source() {

    private val randomAccessFile = RandomAccessFile(file, "r")
    private val fileDescriptor = randomAccessFile.fd

    override val magic: Int
        get() {
            if (magicNum == 0) {
                magicNum = randomAccessFile.readInt()
                randomAccessFile.seek(0)
            }
            return magicNum
        }

    override val orientation: Int
        get() = HeaderParser.getOrientation(FileInputStream(file))

    override fun decode(options: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)
    }

    override fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, options)
    }

    override fun close() {
        randomAccessFile.close()
    }

}

class StreamSource constructor(inputStream: InputStream) : Source() {

    private val recycledInputStream = RecycledInputStream(inputStream)

    override val magic: Int
        @Throws(IOException::class)
        get() {
            if (magicNum == 0) {
                magicNum = HeaderParser.readInt(recycledInputStream)
                Log.d("AoyunTAG", "magicNum: $magicNum")
                recycledInputStream.rewind()
            }
            return magicNum
        }

    override val orientation: Int
        get() = HeaderParser.getOrientation(recycledInputStream)

    override fun decode(options: BitmapFactory.Options): Bitmap? {
        if (options.inTempStorage == null) {
            options.inTempStorage = ByteArrayPool.basicArray
        }
        return if (options.inJustDecodeBounds) {
            recycledInputStream.mark(Integer.MAX_VALUE)
            val bitmap = BitmapFactory.decodeStream(recycledInputStream, null, options)
            recycledInputStream.rewind()
            bitmap
        } else {
            BitmapFactory.decodeStream(recycledInputStream, null, options)
        }

    }

    override fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap? {
        return BitmapFactory.decodeStream(recycledInputStream, rect, options)
    }


    override fun close() {
        recycledInputStream.close()
    }


}