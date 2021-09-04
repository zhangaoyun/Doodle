package me.aoyun.doodle

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import java.io.*

abstract class Source : Closeable {

    // 魔数，提供文件格式的信息
    abstract val magic: Int
    var magicNum: Int = 0

    // 旋转方向，EXIF专属信息
    abstract val orientation: Int

    abstract fun decode(options: BitmapFactory.Options): Bitmap?

    abstract fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap?
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

class StreamSource : Source() {
    override val magic: Int
        get() = TODO("Not yet implemented")
    override val orientation: Int
        get() = TODO("Not yet implemented")

    override fun decode(options: BitmapFactory.Options): Bitmap? {
        TODO("Not yet implemented")
    }

    override fun decodeRegion(rect: Rect, options: BitmapFactory.Options): Bitmap? {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}