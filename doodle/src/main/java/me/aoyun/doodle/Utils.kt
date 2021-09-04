package me.aoyun.doodle

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import com.horizon.doodle.worker.lifecycle.LifeEvent
import com.horizon.doodle.worker.lifecycle.LifecycleManager
import java.io.Closeable
import java.io.File

object Utils {
    private val HEX_DIGITS =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    val context: Context by lazy {
        val ctx = DoodleContentProvider.ctx.applicationContext
        registerActivityLifecycle(ctx)
        ctx
    }

    val cacheDir: String by lazy {
        context.cacheDir.path
    }

    fun long2Hex(num: Long): String {
        var a = num
        val buf = CharArray(16)
        for (i in 7 downTo 0) {
            val index = i shl 1
            val b = (a and 0xFF).toInt()
            buf[index] = HEX_DIGITS[b and 0xF0 shr 4]
            buf[index + 1] = HEX_DIGITS[b and 0xF]
            a = a.ushr(8)
        }
        return String(buf)
    }


    fun makeFileIfNotExist(file: File): Boolean {
        return if (file.exists()) {
            true
        } else {
            val parentFile = file.parentFile
            parentFile != null && (parentFile.isDirectory || parentFile.mkdir()) && file.createNewFile()
        }
    }

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

    private fun registerActivityLifecycle(context: Context) {
        if (context !is Application) {
            return
        }
        context.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
                LifecycleManager.notify(activity, LifeEvent.SHOW)
            }

            override fun onActivityPaused(activity: Activity) {
                LifecycleManager.notify(activity, LifeEvent.HIDE)
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                LifecycleManager.notify(activity, LifeEvent.DESTROY)
            }

        })
    }
}
