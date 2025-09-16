package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R

class DocumentImageAdapter(
    private val urls: List<String>
) : RecyclerView.Adapter<DocumentImageAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFileType: TextView = itemView.findViewById(R.id.imgFileType)
        val txtFileName: TextView = itemView.findViewById(R.id.txtFileName)
        val txtFileSize: TextView = itemView.findViewById(R.id.txtFileSize)
        val relativelayout_header: RelativeLayout = itemView.findViewById(R.id.relativelayout_header)
        val childrelative_layout: RelativeLayout = itemView.findViewById(R.id.childrelative_layout)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fileview_item, parent, false)
        return FileViewHolder(view)

    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.relativelayout_header.visibility = View.GONE
        holder.childrelative_layout.visibility = View.VISIBLE
        val url = urls[position]


        val fileName = url.substringAfterLast("/")
        holder.txtFileName.text = fileName


        val extension = fileName.substringAfterLast(".", "").uppercase()
        holder.imgFileType.text = when (extension) {
            "JPG", "JPEG", "PNG", "GIF" -> "IMG"
            "PDF" -> "PDF"
            "DOC", "DOCX" -> "DOC"
            "XLS", "XLSX" -> "XLS"
            "MP4", "AVI", "MKV" -> "VID"
            "MP3", "WAV" -> "AUD"
            else -> extension.ifEmpty { "FILE" }
        }



        val typeText = when (holder.imgFileType.text) {
            "IMG" -> "IMAGE"
            "PDF" -> "PDF Document"
            "DOC" -> "Word Document"
            "XLS" -> "Excel Sheet"
            "VID" -> "Video"
            "AUD" -> "Audio"
            else -> "File"
        }
        holder.txtFileSize.text = "Unknown Size, $typeText"
    }

    override fun getItemCount(): Int = urls.size
}
