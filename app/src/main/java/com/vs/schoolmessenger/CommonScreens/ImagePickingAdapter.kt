package com.vs.schoolmessenger.CommonScreens

import android.content.Context
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

        when (item.type) {
            FileType.IMAGE -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.add_image)
                    .into(holder.img)
            }

            FileType.PDF -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.pdf_icon)
                    .into(holder.img)
            }

            FileType.DOC -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.doc_icon)
                    .into(holder.img)
            }

            FileType.PPT -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.ppt_icon)
                    .into(holder.img)
            }

            FileType.EXCEL -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.excel_icon)
                    .into(holder.img)
            }

            FileType.TXT -> {
                Glide.with(context)
                    .load(File(item.path))
                    .placeholder(R.drawable.txt_icon)
                    .into(holder.img)
            }

            else -> {

            }
        }

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
