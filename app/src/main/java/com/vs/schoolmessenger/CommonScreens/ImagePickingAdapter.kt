package com.vs.schoolmessenger.CommonScreens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class ImagePickingAdapter(private val imageList: MutableList<ImagePickingData>,val context: Context, private val listener: OnImageClickListener) :
    RecyclerView.Adapter<ImagePickingAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgPicking)
        val imgDelete: ImageView = itemView.findViewById(R.id.imgDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.image_picking_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = imageList[position]
        holder.imageView.setImageResource(imageItem.isImage)
        if (position == 0) {
            holder.imgDelete.visibility = View.GONE
        } else {
            holder.imgDelete.visibility = View.VISIBLE
        }
        holder.imgDelete.setOnClickListener {
            imageList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, imageList.size)
        }
        holder.imageView.setOnClickListener {
            listener.onImageClick(position) // Notify listener about the click
        }

    }

    override fun getItemCount(): Int = imageList.size
}