package com.vs.schoolmessenger.Parent.QuizExam.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R

class CircuitImageAdapter(
    private val fileList: MutableList<String>,
    private val onItemClick: (Int) -> Unit,
) : RecyclerView.Adapter<CircuitImageAdapter.ImageViewHolder>() {

    fun updateData(newList: List<String>) {
        fileList.clear()
        fileList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivCircuitImage)
        val playOverlay: ImageView = itemView.findViewById(R.id.ivPlayOverlay)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_circuit_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val url = fileList[position].lowercase()

        when {
            url.endsWith(".pdf") -> {
                holder.imageView.setImageResource(R.drawable.pdf_icon)
                holder.playOverlay.visibility = View.GONE
            }
            url.endsWith(".mp4") || url.contains("video") -> {
                Glide.with(holder.imageView.context)
                    .load(fileList[position])
                    .placeholder(R.drawable.upload_image)
                    .error(R.drawable.upload_image)
                    .into(holder.imageView)

                holder.playOverlay.visibility = View.VISIBLE
            }
            else -> {
                Glide.with(holder.imageView.context)
                    .load(fileList[position])
                    .placeholder(R.drawable.upload_image)
                    .error(R.drawable.upload_image)
                    .into(holder.imageView)

                holder.playOverlay.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = fileList.size
}

//package com.vs.schoolmessenger.Parent.QuizExam.Adapter


//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.vs.schoolmessenger.R
//
//class CircuitImageAdapter(
//    private val imageList: MutableList<String>,
//    private val onImageClick: (Int) -> Unit,
//) : RecyclerView.Adapter<CircuitImageAdapter.ImageViewHolder>() {
//
//    fun updateData(newList: List<String>) {
//        imageList.clear()
//        imageList.addAll(newList)
//        notifyDataSetChanged()
//    }
//
//    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.ivCircuitImage)
//
//        init {
//            itemView.setOnClickListener {
//                if (adapterPosition != RecyclerView.NO_POSITION) {
//                    onImageClick(adapterPosition)
//                }
//            }
//        }
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_circuit_image, parent, false)
//        return ImageViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
//        Glide.with(holder.imageView.context)
//            .load(imageList[position])
//            .placeholder(R.drawable.upload_image)
//            .error(R.drawable.upload_image)
//            .into(holder.imageView)
//    }
//
//    override fun getItemCount(): Int = imageList.size
//}
