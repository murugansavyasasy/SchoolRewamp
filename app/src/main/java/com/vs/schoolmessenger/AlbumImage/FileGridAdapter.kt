package com.vs.schoolmessenger.AlbumImage

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.ItemFileBinding

// FileGridAdapter.kt
class FileGridAdapter(
    private val files: List<FileItem>,
    private val onFileToggle: (Uri, Boolean) -> Unit
) : RecyclerView.Adapter<FileGridAdapter.FileViewHolder>() {

    private val selectedItems = mutableSetOf<Uri>()

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val item = files[position]
        val uri = item.uri
        val context = holder.binding.imageThumb.context

        if (item.mimeType!!.startsWith("image") || item.mimeType.startsWith("video")) {
            Glide.with(context).load(uri).centerCrop().into(holder.binding.imageThumb)
        } else if (item.mimeType == "application/pdf") {
            holder.binding.imageThumb.setImageResource(R.drawable.hw_pdf_img)
        } else {
            holder.binding.imageThumb.setImageResource(R.drawable.daily_collection)
        }

        holder.binding.fileName.text = item.name
        holder.binding.root.alpha = if (selectedItems.contains(uri)) 0.5f else 1.0f

        holder.binding.root.setOnClickListener {
            val isSelected = selectedItems.contains(uri)
            if (isSelected) selectedItems.remove(uri) else selectedItems.add(uri)
            notifyItemChanged(position)
            onFileToggle(uri, !isSelected)
        }
    }

    override fun getItemCount() = files.size
}
