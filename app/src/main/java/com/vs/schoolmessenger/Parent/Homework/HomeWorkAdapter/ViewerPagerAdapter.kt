package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class ViewerPagerAdapter(
    private val fileList: ArrayList<GetFilePathDetails>,
    private val subjectName: String,
    private val context: Context,
) : RecyclerView.Adapter<ViewerPagerAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.homework_view_image_document_item, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        val file = fileList[position]

        when (file.type.uppercase()) {
            Constant.IMAGE -> {
                Glide.with(context)
                    .load(file.path)
                    .into(holder.imageView)
            }
        }

        holder.subjectName.text=subjectName


    }


    override fun getItemCount(): Int = fileList.size

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.fullScreenImageView)
        val subjectName: TextView = itemView.findViewById(R.id.lblsubject)
    }
}
