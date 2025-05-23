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
        if (isImage(uri, context)) {
            Glide.with(context)
                .load(uri)
                .into(binding.imageView)
        } else if (isVideo(uri, context)) {
            Glide.with(context)
                .load(uri)
                .into(binding.imageView)
            binding.videoIcon.visibility = View.VISIBLE
        } else if (isAudio(uri, context)) {
            binding.imageView.setImageResource(R.drawable.voice)
            binding.audioIcon.visibility = View.VISIBLE
        } else {
            binding.imageView.setImageResource(R.drawable.doc_icon)
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

    override fun getItemCount(): Int = items.size

    private fun isImage(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("image/") == true
    }

    private fun isVideo(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("video/") == true
    }

    private fun isAudio(uri: Uri, context: Context): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType?.startsWith("audio/") == true
    }
}