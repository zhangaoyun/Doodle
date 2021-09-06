package me.aoyun.doodle

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import okhttp3.CacheControl
import java.io.*

abstract class Source : Closeable {

    // 魔数，提供文件格式的信息
    abstract val magic: Int
    var magicNum: Int = 0

    // 旋转方向，EXIF专属信息
    abstract val orientation: Int

    abstract val data: ByteArray

    abstract fun decode(options: BitmapFactory.Options): Bitmap?

    abstract fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap?

    companion object {

        private const val ASSET_PREFIX = "file:///android_asset/"
        private const val ASSET_PREFIX_LENGTH = ASSET_PREFIX.length
        private const val FILE_PREFIX = "file://"
        private const val FILE_PREFIX_LENGTH = FILE_PREFIX.length

        fun valueOf(src: Any?): Source {
            if (src == null) {
                throw IllegalArgumentException("source is null")
            }
            return when (src) {
                is File -> FileSource(src)
                is InputStream -> StreamSource(src)
                is AssetManager.AssetInputStream -> AssetSource(src)
                else -> throw IllegalArgumentException("Unsupported src " + src.javaClass.simpleName)
            }
        }

        fun parse(request: Request): Source {
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
                path.startsWith(ASSET_PREFIX) -> {
                    valueOf(
                        Utils.context.assets.open(
                            path.substring(ASSET_PREFIX_LENGTH)
                        )
                    )
                }
                path.startsWith(FILE_PREFIX) -> valueOf(File(path.substring(FILE_PREFIX_LENGTH)))
                else -> valueOf(
                    Utils.context.contentResolver.openInputStream(
                        (request.uri
                            ?: Uri.parse(path))
                    )
                )
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

    override val data: ByteArray
        get() = ByteArrayPool.loadData(FileInputStream(file))

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

    override val data: ByteArray
        get() = ByteArrayPool.loadData(recycledInputStream)

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

class AssetSource(private val assetStream: AssetManager.AssetInputStream) : Source() {

    override val magic: Int
        get() {
            if (magicNum == 0) {
                assetStream.mark(4)
                magicNum = HeaderParser.readInt(assetStream)
                assetStream.reset()
            }
            return magicNum
        }

    override val orientation: Int
        get() = HeaderParser.getOrientation(assetStream)

    override val data: ByteArray
        get() = ByteArrayPool.loadData(assetStream)

    override fun decode(options: BitmapFactory.Options): Bitmap? {
        if (options.inJustDecodeBounds) {
            assetStream.mark(Integer.MAX_VALUE)
            val bitmap = BitmapFactory.decodeStream(assetStream, null, options)
            assetStream.reset()
            return bitmap
        }
        return BitmapFactory.decodeStream(assetStream, null, options)
    }

    override fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap? {
        return BitmapRegionDecoder.newInstance(assetStream, false).decodeRegion(rect, options)
    }

    override fun close() {
        assetStream.close()
    }

}