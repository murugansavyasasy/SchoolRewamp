package com.vs.schoolmessenger.Parent.Noticeboard.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Noticeboard.FilePath
import com.vs.schoolmessenger.R

class NoticeViewerPagerAdapter(
    private val fileList: ArrayList<FilePath>,

    private val context: Context,
) : RecyclerView.Adapter<NoticeViewerPagerAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.homework_view_image_document_item, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        fileList[position]
//        when (file.type.uppercase()) {
//            Constant.IMAGE -> {
//                Glide.with(context)
//                    .load(file.url)
//                    .into(holder.imageView)
//            }
//            else -> {
//
//            }
//        }
    }

    override fun getItemCount(): Int = fileList.size

    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // val imageView: ImageView = itemView.findViewById(R.id.fullScreenImageView)
    }
}
