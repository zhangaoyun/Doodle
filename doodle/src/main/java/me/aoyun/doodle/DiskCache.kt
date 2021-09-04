package me.aoyun.doodle

import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object DiskCache {

    private var journalEnd: Int = 0
    private const val PAGE_SIZE: Long = 4096L // 4k
    private const val JOURNAL_NAME = "journal"

    private lateinit var cachePath: String

    private lateinit var channel: FileChannel
    private lateinit var buffer: MappedByteBuffer

    private val journals: HashMap<Long, JournalValue> by lazy {
        val diskCachePath = Config.diskCachePath
        cachePath = if (diskCachePath.isEmpty()) {
            Utils.cacheDir + "/doodle/result/"
        } else {
            diskCachePath
        }
        HashMap<Long, JournalValue>().apply {
            readJournal(this)
            // update access time
            checkFile(this)
        }
    }

    private fun checkFile(map: HashMap<Long, JournalValue>) {

    }

    /**
     * @return 图片文件的路径，不存在返回 null
     */
    fun get(key: Long): String? {
        var journalValue: JournalValue?
        synchronized(this) {
            journalValue = journals[key]?.apply {
                accessTime = System.currentTimeMillis()
                buffer.putLong(offset, accessTime)
            }
        }
        return if (journalValue != null) {
            keyToPath(key)
        } else {
            null
        }
    }

    private fun keyToPath(key: Long): String {
        return cachePath + Utils.long2Hex(key)
    }

    fun put(key: Long, bitmap: Bitmap) {
        if (Config.diskCacheCapacity <= 0) {
            return
        }
        var value: JournalValue?
        synchronized(this) {
            value = journals[key]
        }
        if (value == null) {
            val filename = keyToPath(key)
            val tmpFile = File("$filename.tmp")
            if (Utils.makeFileIfNotExist(tmpFile)) {
                saveBitmap(tmpFile, bitmap)
                val fileLen = tmpFile.length()
                if (!tmpFile.renameTo(File(filename))) {
                    return
                }
                addJournal(key, fileLen)
                // TODO checksize
            }
        }
    }

    private fun addJournal(key: Long, fileLen: Long) {
        val accessTime = System.currentTimeMillis()
        val offset = appendJournal(key, accessTime)
        journals[key] = JournalValue(key, accessTime, fileLen, offset)
    }

    /**
     * @return accessTime 的 offset
     */
    private fun appendJournal(key: Long, accessTime: Long): Int {
        val end = journalEnd
        if (end + 16 > buffer.capacity()) {
            buffer.force()
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, (end + PAGE_SIZE))
        }
        buffer.position(end)
        buffer.putLong(key)
        buffer.putLong(accessTime)
        journalEnd += 16
        return end + 8
    }


    private fun saveBitmap(tmpFile: File, bitmap: Bitmap) {
        val fileOutputStream = FileOutputStream(tmpFile)
        bitmap.compress(Config.compressFormat, 100, fileOutputStream)
        // 强制
        fileOutputStream.flush()
        Utils.closeQuietly(fileOutputStream)
    }

    /**
     * 记录日志，方案要点如下：
    1、一条记录存储key(long)和最近访问时间(long)，一条记录16字节；
    2、每条记录依次排列，由于比较规整，可以根据偏移量随机读写；
    3、用mmap方式映射日志文件，以4K为单位映射。
     */
    class JournalValue(
        val key: Long,
        var accessTime: Long,
        val fileLength: Long,
        var offset: Int
    ) : Comparable<JournalValue> {
        override fun compareTo(other: JournalValue): Int {
            return accessTime.compareTo(other.accessTime)
        }

    }

    private fun readJournal(map: HashMap<Long, JournalValue>) {
        val journalFile = File(cachePath + JOURNAL_NAME)
        if (Utils.makeFileIfNotExist(journalFile)) {
            var invalidCount = 0
            val randomAccessFile = RandomAccessFile(journalFile, "rw")
            val length = alignLength(randomAccessFile.length())

            channel = randomAccessFile.channel
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, length)
            buffer.position(0)
            while (buffer.hasRemaining()) {
                val key = buffer.long
                if (key == 0L) {
                    journalEnd = buffer.position() - 8
                    break
                }
                val accessTime = buffer.long
                if (accessTime > 0) {
                    map[key] = JournalValue(key, accessTime, 0, buffer.position() - 8)
                } else {
                    // 缓存过期
                    invalidCount++
                }
            }

            if (buffer.position() == buffer.capacity()) {
                journalEnd = buffer.position()
            }

            if (invalidCount * 16 >= PAGE_SIZE) {
                // make gc for journal
                alignFile(ArrayList(map.values), 0)
            }
        }

    }

    private fun alignFile(journalList: ArrayList<JournalValue>, start: Int) {
        val count = journalList.size
        var i = start

        val newLen = alignLength(((count - 1) * 16).toLong())
        if (newLen < buffer.capacity()) {
            // 强制写入磁盘
            buffer.force()
            channel.truncate(newLen)
            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, newLen)
        }
        buffer.clear()
        while (i < count) {
            val journalValue = journalList[i]
            buffer.putLong(journalValue.key)
            journalValue.offset = buffer.position()
            buffer.putLong(journalValue.accessTime)

        }
        journalEnd = buffer.position()

        while (buffer.hasRemaining()) {
            buffer.putLong(0)
        }
    }

    private fun alignLength(length: Long): Long {
        return when {
            length <= 0 -> PAGE_SIZE
            ((length and 0xFFF) != 0L) -> length + PAGE_SIZE shr 12 shl 12
            else -> PAGE_SIZE
        }
    }
}