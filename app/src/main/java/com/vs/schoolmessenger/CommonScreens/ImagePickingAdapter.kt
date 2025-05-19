package com.vs.schoolmessenger.CommonScreens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File

class ImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImagePickingAdapter.FileViewHolder>() {

    inner class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgPicking)
        val del: ImageView = v.findViewById(R.id.imgDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_picking_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, pos: Int) {
        val item = items[pos]
        Log.d("isFileType", item.type.toString())
        Log.d("isFilePath", item.path.toString())

        val filePath = item.path
        val fileUri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(filePath)
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
            FileType.VIDEO -> R.drawable.video_icon
            FileType.AUDIO -> R.drawable.voice
            else -> R.drawable.address_icon
        }

        Glide.with(context)
            .load(fileUri)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .into(holder.img)


//        when (item.type) {
//            FileType.IMAGE -> {
//                val imageSource =
//                    if (item.path.startsWith("content://") || item.path.startsWith("file://")) {
//                        Uri.parse(item.path)
//                    } else {
//                        File(item.path)
//                    }
//
//                Glide.with(context)
//                    .load(imageSource)
//                    .into(holder.img)
//
//            }
//
//            FileType.PDF -> {
//                Glide.with(context)
//                    .load(File(item.path))
//                    .placeholder(R.drawable.pdf_icon)
//                    .into(holder.img)
//            }
//
//            FileType.DOC -> {
//                Glide.with(context)
//                    .load(File(item.path))
//                    .placeholder(R.drawable.doc_icon)
//                    .into(holder.img)
//            }
//
//            FileType.PPT -> {
//                Glide.with(context)
//                    .load(File(item.path))
//                    .placeholder(R.drawable.ppt_icon)
//                    .into(holder.img)
//            }
//
//            FileType.EXCEL -> {
//                Glide.with(context)
//                    .load(File(item.path))
//                    .placeholder(R.drawable.excel_icon)
//                    .into(holder.img)
//            }
//
//            FileType.TXT -> {
//                Glide.with(context)
//                    .load(File(item.path))
//                    .placeholder(R.drawable.txt_icon)
//                    .into(holder.img)
//            }
//
//            else -> {
//
//            }
//        }

        holder.del.visibility = if (pos == 0) GONE else VISIBLE
        holder.del.setOnClickListener {
            items.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, items.size)
        }

        holder.itemView.setOnClickListener {
            if (pos != 0) {

            } else {
                listener.onImageClick(pos)
            }
        }
    }

    override fun getItemCount() = items.size
}
