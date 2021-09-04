package me.aoyun.doodle

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SingleImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_image)

        val imageView = findViewById<ImageView>(R.id.imageView)
//        Doodle.load("https://www.wanandroid.com/blogimgs/50c115c2-cf6c-4802-aa7b-a4334de444cd.png")
//            .into(imageView)

        val path = "file:///android_asset/lenna.jpg"
        Doodle.load(path)
            .into(imageView)
    }
}