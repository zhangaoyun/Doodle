package me.aoyun.doodle

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class GridViewAdapter(private val context: Context) : BaseAdapter() {

    private val urls: List<String>

    init {
        val tmpList = Data.URLS.toMutableList()
        tmpList.shuffle()

        urls = listOf(tmpList, tmpList, tmpList).flatten()
    }

    override fun getCount(): Int {
        return urls.size
    }

    override fun getItem(position: Int): String {
        return urls[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = convertView as? SquaredImageView ?: SquaredImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val url = getItem(position)

        Doodle.load(url)
            .placeholder(R.drawable.placeholder)
            .into(imageView)

        return imageView
    }
}