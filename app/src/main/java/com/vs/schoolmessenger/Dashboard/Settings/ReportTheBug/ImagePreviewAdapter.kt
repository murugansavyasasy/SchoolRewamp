package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File

class ImagePreviewAdapter(
    private val imagePathList: MutableList<FileItem>,
    private val context: Context,
    private val listener: ImagePreviewRemoveListener
) : BaseAdapter() {

    override fun getCount(): Int = imagePathList.size
    override fun getItem(position: Int): Any = imagePathList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.image_preview, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = imagePathList[position]

//        Glide.with(context)
//            .load(Uri.parse(item.path))
//            .into(holder.imgGallery)
        holder.imgCancel.tag = position
        holder.imgCancel.setOnClickListener {
            val pos = it.tag as Int   // Correct position every time
            listener.remove(pos)
        }

        val filePath = item.path
        val fileUri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(
                filePath
            )

            filePath.startsWith("http://") || filePath.startsWith("https://") -> filePath
            else -> File(filePath)
        }

        val placeholderRes = when (item.type) {
            FileType.PDF -> R.drawable.pdf_icon
            FileType.DOC, FileType.DOCX -> R.drawable.doc_icon
            FileType.PPT -> R.drawable.ppt_icon
            FileType.EXCEL -> R.drawable.excel_icon
            FileType.TXT -> R.drawable.txt_icon
            FileType.IMAGE -> R.drawable.image_placeholder
            FileType.VIDEO -> R.drawable.black
            FileType.AUDIO -> R.drawable.voice
            else -> R.drawable.address_icon
        }

        Glide.with(context)
            .load(fileUri)
            .placeholder(placeholderRes)
            .apply(RequestOptions().dontTransform())
            .error(placeholderRes)
            .into(holder.imgGallery)

        if (item.type.toString() == Constant.VIDEO) {
            holder.imgVideo.visibility = VISIBLE
        } else {
            holder.imgVideo.visibility = GONE
        }

        return view
    }

    class ViewHolder(view: View) {
        val imgGallery: ImageView = view.findViewById(R.id.imgGallery)
        val imgVideo: ImageView = view.findViewById(R.id.imgVideo)
        val imgCancel: ImageView = view.findViewById(R.id.imgCancle)
    }
}

