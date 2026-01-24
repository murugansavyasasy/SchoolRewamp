package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class DocumentImageAdapter(
    private val context: Context,
    private val files: List<CommonFileData>,
    private val isSubjectName: String
) : RecyclerView.Adapter<DocumentImageAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFileType: TextView = itemView.findViewById(R.id.imgFileType)
        val txtFileName: TextView = itemView.findViewById(R.id.txtFileName)
        val txtFileSize: TextView = itemView.findViewById(R.id.txtFileSize)
        val relativelayout_header: RelativeLayout =
            itemView.findViewById(R.id.relativelayout_header)
        val childrelative_layout: RelativeLayout = itemView.findViewById(R.id.childrelative_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.fileview_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.relativelayout_header.visibility = View.GONE
        holder.childrelative_layout.visibility = View.VISIBLE

        val file = files[position]
        val fileName = file.path.substringAfterLast("/")
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
        holder.txtFileSize.text = "${context.getString(R.string.Unknown_Size)}, $typeText"

        val clickListener = View.OnClickListener {
            Constant.commonFileList = files.toMutableList()
            Constant.selectedFileIndex = position
            val intent = Intent(context, FilesViewActivity::class.java)
            intent.putExtra(Constant.subjectName, isSubjectName)
            context.startActivity(intent)
        }

        holder.relativelayout_header.setOnClickListener(clickListener)
        holder.childrelative_layout.setOnClickListener(clickListener)
    }

    override fun getItemCount(): Int = files.size
}
