package me.aoyun.doodle

import java.io.File

interface CacheInterceptor {

    fun cachePath(path: String): File?
}