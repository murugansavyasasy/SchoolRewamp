package com.vs.schoolmessenger.AlbumImage

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ItemFileBinding

class FileGridAdapter(
    private val limit: Int,
    private val onSelectionChanged: (List<Uri>) -> Unit
) : RecyclerView.Adapter<FileGridAdapter.FileViewHolder>() {

    private val selected = mutableListOf<Uri>()
    private val items = mutableListOf<Uri>()

    fun submitList(list: List<Uri>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val uri = items[position]
        val binding = holder.binding
        val context = binding.root.context

        binding.audioIcon.visibility = View.GONE
        binding.videoIcon.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        val ext = getFileExtension(context, uri)

        println("LOADED FILE: $uri -> TYPE: $ext")

        when (ext) {
            "image" -> {
                Glide.with(context).load(uri).into(binding.imageView)
            }
            "video" -> {
                Glide.with(context).load(uri).into(binding.imageView)
                binding.videoIcon.visibility = View.VISIBLE
            }
            "audio" -> {
                binding.imageView.setImageResource(R.drawable.voice)
                binding.audioIcon.visibility = View.VISIBLE
            }
            "pdf" -> binding.imageView.setImageResource(R.drawable.pdf_icon)
            "doc" -> binding.imageView.setImageResource(R.drawable.doc_icon)
            "xls" -> binding.imageView.setImageResource(R.drawable.excel_icon)
            "ppt" -> binding.imageView.setImageResource(R.drawable.ppt_icon)
            "txt" -> binding.imageView.setImageResource(R.drawable.txt_icon)
            else -> binding.imageView.setImageResource(R.drawable.doc_icon)
        }

        binding.progressBar.visibility = View.GONE
        binding.checkIcon.visibility = if (selected.contains(uri)) View.VISIBLE else View.GONE

        binding.root.setOnClickListener {
            if (selected.contains(uri)) {
                selected.remove(uri)
            } else {
                if (selected.size >= limit) {
                    Toast.makeText(context, "Limit is $limit", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                selected.add(uri)
            }
            notifyItemChanged(position)
            onSelectionChanged(selected)
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        // Try MIME type first
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) {
            return when {
                mimeType.startsWith("image/") -> "image"
                mimeType.startsWith("video/") -> "video"
                mimeType.startsWith("audio/") -> "audio"
                mimeType == "application/pdf" -> "pdf"
                mimeType == "application/msword" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "doc"
                mimeType == "application/vnd.ms-excel" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                        mimeType == "application/x-tika-msoffice" -> "xls"
                mimeType == "application/vnd.ms-powerpoint" ||
                        mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "ppt"
                mimeType == "text/plain" -> "txt"
                else -> {
                    println("UNKNOWN MIME: $mimeType")
                    "unknown"
                }
            }
        }
        val path = uri.toString().lowercase()
        return when {
            path.endsWith(".pdf") -> "pdf"
            path.endsWith(".doc") || path.endsWith(".docx") -> "doc"
            path.endsWith(".xls") || path.endsWith(".xlsx") -> "xls"
            path.endsWith(".ppt") || path.endsWith(".pptx") -> "ppt"
            path.endsWith(".txt") -> "txt"
            path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") -> "image"
            path.endsWith(".mp4") || path.endsWith(".mkv") -> "video"
            path.endsWith(".mp3") || path.endsWith(".wav") -> "audio"
            else -> {
                println("UNKNOWN EXT: $path")
                "unknown"
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
