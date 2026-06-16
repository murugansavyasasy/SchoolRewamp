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
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<CircuitImageAdapter.ViewHolder>() {

    fun updateData(newList: List<String>) {
        fileList.clear()
        fileList.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivCircuitImage)
        val ivPlay: ImageView = itemView.findViewById(R.id.ivPlayOverlay)
        val ivPdf: ImageView = itemView.findViewById(R.id.ivPdfOverlay)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_circuit_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = fileList[position].lowercase()

        holder.ivPlay.visibility = View.GONE
        holder.ivPdf.visibility = View.GONE

        when {
            url.endsWith(".pdf") -> {
                holder.ivPdf.visibility = View.VISIBLE
            }

            url.endsWith(".mp4") || url.contains("video") -> {
                Glide.with(holder.ivImage.context)
                    .load(fileList[position])
                    .placeholder(R.drawable.upload_image)
                    .error(R.drawable.upload_image)
                    .into(holder.ivImage)
                holder.ivPlay.visibility = View.VISIBLE
            }

            else -> {
                Glide.with(holder.ivImage.context)
                    .load(fileList[position])
                    .placeholder(R.drawable.upload_image)
                    .error(R.drawable.upload_image)
                    .into(holder.ivImage)
            }
        }
    }

    override fun getItemCount() = fileList.size
}