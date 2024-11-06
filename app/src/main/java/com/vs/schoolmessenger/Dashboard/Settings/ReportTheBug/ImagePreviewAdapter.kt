package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import com.vs.schoolmessenger.R
import java.io.File

class ImagePreviewAdapter(
    private val imagePathList: ArrayList<String>?,
    private val context: Context,
    var param: ImagePreviewRemoveListener
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var cardImage: CardView
    private lateinit var imgGallery: ImageView
    private lateinit var imgCancle: ImageView

    companion object {
        var checkClick: ImagePreviewRemoveListener? = null
    }

    override fun getCount(): Int {
        return imagePathList!!.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // in below function we are getting individual item of grid view.
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var convertView = convertView
        checkClick = param
        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.image_preview, null)
        }

        imgGallery = convertView!!.findViewById(R.id.imgGallery)
        imgCancle = convertView.findViewById(R.id.imgCancle)
        imgCancle!!.visibility = View.VISIBLE
        val isImagePreview = imagePathList?.get(position)
        if (imagePathList!![position].contains("amazonaws")) {
            Picasso.get().load(isImagePreview!!).into(imgGallery)
        } else {
            val isImage = File(isImagePreview!!)
            val imageUri = Uri.fromFile(isImage)
            imgGallery.setImageURI(imageUri)
        }


        imgCancle.setOnClickListener {
            imagePathList.removeAt(position)
            checkClick?.remove(position)
            notifyDataSetChanged()
        }

        return convertView
    }
}