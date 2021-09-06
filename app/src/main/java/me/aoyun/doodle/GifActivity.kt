package me.aoyun.doodle

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class GifActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif)

        val gifImage = findViewById<ImageView>(R.id.gif_image)
        Doodle.load(R.raw.fish)
            .into(gifImage)
    }
}