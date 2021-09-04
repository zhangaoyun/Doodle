package me.aoyun.doodle

import android.widget.ImageView

class Request {

    private val path: String
    private val key: Long = MHash.hash64(toString())

    constructor(path: String) {
        this.path = path
    }

    fun into(target: ImageView) {
        fillSizeAndLoad(target.width, target.height)
    }

    private fun fillSizeAndLoad(width: Int, height: Int) {
        Dispatcher.start(this)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        return stringBuilder.toString()
    }
}