package me.aoyun.doodle

import android.graphics.Bitmap

object DiskCache {

    fun get(key: Long): String? {

    }

    fun put(key: Long, bitmap: Bitmap) {

    }

    /**
     * 记录日志，方案要点如下：
    1、一条记录存储key(long)和最近访问时间(long)，一条记录16字节；
    2、每条记录依次排列，由于比较规整，可以根据偏移量随机读写；
    3、用mmap方式映射日志文件，以4K为单位映射。
     */
    class JournalValue(
        val key: Long,
        val accessTime: Long,
        val fileLength: Long,
        val offset: Int
    ) : Comparable<JournalValue> {
        override fun compareTo(other: JournalValue): Int {
            return accessTime.compareTo(other.accessTime)
        }

    }
}