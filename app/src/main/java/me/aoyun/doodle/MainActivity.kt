package me.aoyun.doodle

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonToSingle = findViewById<Button>(R.id.buttonToSingle)
        buttonToSingle.setOnClickListener {
            startActivity(Intent(this, SingleImageActivity::class.java))
        }


    }
}