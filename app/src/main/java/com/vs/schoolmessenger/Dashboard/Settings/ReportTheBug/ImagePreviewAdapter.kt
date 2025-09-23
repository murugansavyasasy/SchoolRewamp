package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import java.io.File

class ImagePreviewAdapter(
    private val imagePathList: ArrayList<String>,
    private val context: Context,
    private val listener: ImagePreviewRemoveListener
) : BaseAdapter() {

    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = imagePathList.size

    override fun getItem(position: Int): Any = imagePathList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.image_preview, parent, false)

        val imgGallery = view.findViewById<ImageView>(R.id.imgGallery)
        val imgCancel = view.findViewById<ImageView>(R.id.imgCancle)

        val path = imagePathList[position]

        Glide.with(context)
            .load(Uri.parse(path))
            .into(imgGallery)

        imgCancel.setOnClickListener {
            listener.remove(position)
        }

        return view
    }
}
