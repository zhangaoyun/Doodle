package me.aoyun.doodle

import android.os.Bundle
import android.widget.GridView
import androidx.appcompat.app.AppCompatActivity

class GridViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        findViewById<GridView>(R.id.grid_view).apply {
            adapter = GridViewAdapter(this@GridViewActivity)
        }
    }
}