package com.vs.schoolmessenger.AlbumImage

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
    private val onSelectionChanged: (List<Uri>) -> Unit,
    private val onItemClicked: (Uri) -> Unit
) : RecyclerView.Adapter<FileGridAdapter.FileViewHolder>() {

    private val selected = mutableListOf<Uri>()
    private val items = mutableListOf<Uri>()
    private val videoSizeCache = mutableMapOf<Uri, Long>()
    private val nonSelectableUris = mutableSetOf<Uri>()

    fun submitList(list: List<Uri>) {
        items.clear()
        selected.clear()
        nonSelectableUris.clear()
        videoSizeCache.clear()
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

        val fileName = getFileName(context, uri)
        binding.fileName.text = fileName

        val mimeType = context.contentResolver.getType(uri)
        val filePath = uri.toString()

        var isDisabled = false

        when {
            mimeType?.startsWith("image/") == true -> {
                Glide.with(context).load(uri).into(binding.imageView)
            }

            mimeType?.startsWith("video/") == true -> {
                Glide.with(context).load(uri).into(binding.imageView)
                binding.videoIcon.visibility = View.VISIBLE

                // Check size and disable if too large
                val size = videoSizeCache.getOrPut(uri) { getFileSize(context, uri) }
                val maxSizeInBytes = 500L * 1024 * 1024 // 500 MB

                if (size > maxSizeInBytes) {
                    isDisabled = true
                    nonSelectableUris.add(uri)
                }
            }

            mimeType?.startsWith("audio/") == true -> {
                binding.imageView.setImageResource(R.drawable.voice)
                binding.audioIcon.visibility = View.VISIBLE
            }

            mimeType == "application/pdf" || filePath.endsWith(".pdf") -> {
                binding.imageView.setImageResource(R.drawable.pdf_icon)
            }

            mimeType == "application/msword" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                    filePath.endsWith(".doc") || filePath.endsWith(".docx") -> {
                binding.imageView.setImageResource(R.drawable.doc_icon)
            }

            mimeType == "application/vnd.ms-excel" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                    filePath.endsWith(".xls") || filePath.endsWith(".xlsx") -> {
                binding.imageView.setImageResource(R.drawable.excel_icon)
            }

            mimeType == "application/vnd.ms-powerpoint" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.presentationml.presentation" ||
                    filePath.endsWith(".ppt") || filePath.endsWith(".pptx") -> {
                binding.imageView.setImageResource(R.drawable.ppt_icon)
            }

            mimeType == "text/plain" || filePath.endsWith(".txt") -> {
                binding.imageView.setImageResource(R.drawable.txt_icon)
            }

            else -> {
                binding.imageView.setImageResource(R.drawable.doc_icon)
            }
        }

        binding.progressBar.visibility = View.GONE
        binding.checkIcon.visibility = if (selected.contains(uri)) View.VISIBLE else View.GONE

        // Set alpha for visual feedback
        binding.imageView.alpha = when {
            isDisabled -> 0.3f
            selected.contains(uri) -> 0.5f
            else -> 1.0f
        }

        binding.root.setOnClickListener {
            if (nonSelectableUris.contains(uri)) {
                Toast.makeText(
                    context,
                    "Cannot select videos larger than 500 MB",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            onItemClicked(uri)

            if (selected.contains(uri)) {
                selected.remove(uri)
            } else {
                if (selected.size >= limit) {
                    Toast.makeText(
                        context,
                        "You can select up to $limit items only",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                selected.add(uri)
            }

            notifyItemChanged(position)
            onSelectionChanged(selected)
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name ?: uri.lastPathSegment ?: "Unknown"
    }

    fun getSelectedItems(): List<Uri> = selected.toList()

    override fun getItemCount(): Int = items.size
}
