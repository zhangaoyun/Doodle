package me.aoyun.doodle

import android.graphics.drawable.Drawable

interface GifDecoder {

    fun decode(byteArray: ByteArray): Drawable
}