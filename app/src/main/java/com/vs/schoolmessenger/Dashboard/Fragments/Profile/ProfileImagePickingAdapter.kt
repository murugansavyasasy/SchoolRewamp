package com.vs.schoolmessenger.Dashboard.Fragments.Profile

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.CommonScreens.OnImageClickListener
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

class ProfileImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ProfileImagePickingAdapter.FileViewHolder>() {

    class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val txtFileName: TextView = v.findViewById(R.id.txtFileName)
        val txtFileSize: TextView = v.findViewById(R.id.txtFileSize)
        val imgFileType: TextView = v.findViewById(R.id.imgFileType)
        val imgDelete: ImageView = v.findViewById(R.id.imgDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_image_picking_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, pos: Int) {
        val item = items[pos]


        val file = File(item.path)
        holder.txtFileName.text = file.name


        val typeLabel = when (item.type) {
            FileType.IMAGE -> "IMG"
            FileType.VIDEO -> "VID"
            FileType.PDF -> "PDF"
            FileType.DOC, FileType.DOCX -> "DOC"
            FileType.PPT -> "PPT"
            FileType.EXCEL -> "XLS"
            FileType.TXT -> "TXT"
            FileType.AUDIO -> "AUD"
            else -> "FILE"
        }
        holder.imgFileType.text = typeLabel

        val fileSize = if (file.exists()) {
            getReadableFileSize(file.length())
        } else {
            "0 KB"
        }
        holder.txtFileSize.text = "$fileSize, ${item.type}"


        holder.imgDelete.setOnClickListener {
            Log.d("isPosition", pos.toString())
            items.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, items.size)
        }



        holder.itemView.setOnClickListener {
            if (!item.path.contains("amazonaws.")) {
                if (item.type.toString() == Constant.IMAGE || item.type.toString() == Constant.VIDEO) {
                    val filteredFiles = Constant.selectedFiles.filter {
                        it.type.toString() == Constant.IMAGE || it.type.toString() == Constant.VIDEO
                    }
                    Constant.commonFileList = filteredFiles.map {
                        CommonFileData(
                            type = it.type.toString(),
                            path = it.path
                        )
                    }
                        .toMutableList()
                    val clickedPath = item.path
                    val indexInFiltered = filteredFiles.indexOfFirst { it.path == clickedPath }
                        .let { if (it >= 0) it else 0 }

                    Constant.selectedFileIndex = indexInFiltered
                    val intent = Intent(context, FilesViewActivity::class.java)
                    intent.putExtra(Constant.subjectName, "Your Files")
                    context.startActivity(intent)
//                    Constant.commonFileList = Constant.selectedFiles.map {
//                        CommonFileData(type = it.type.toString(), path = it.path)
//                    }.toMutableList()
//                    Constant.selectedFileIndex = pos
//                    val intent = Intent(context, FilesViewActivity::class.java)
//                    intent.putExtra(Constant.subjectName, "Your Files")
//                    context.startActivity(intent)
                } else {
                    val uri = if (item.path.startsWith("content://")) {
                        Uri.parse(item.path)
                    } else {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            File(item.path)
                        )
                    }

                    val mimeType = getMimeTypeFromUri(uri)
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val activities = context.packageManager.queryIntentActivities(
                        openIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    if (activities.isNotEmpty()) {
                        context.startActivity(Intent.createChooser(openIntent, "Open with"))
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.please_download_an_app_to_view_this_file),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Constant.commonFileList = Constant.selectedFiles.map {
                    CommonFileData(type = it.type.toString(), path = it.path)
                }.toMutableList()
                Constant.selectedFileIndex = pos - 1
                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, "Your Files")
                context.startActivity(intent)
            }
        }

    }


    override fun getItemCount() = items.size

    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "*/*"
    }


    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
