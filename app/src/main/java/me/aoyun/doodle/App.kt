package me.aoyun.doodle

import android.app.Application
import android.graphics.drawable.Drawable
import pl.droidsonroids.gif.GifDrawable

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Config.setGifDecoder(object : GifDecoder{
            override fun decode(byteArray: ByteArray): Drawable {
                return GifDrawable(byteArray)
            }

        })
    }
}