package com.vs.schoolmessenger.CommonScreens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File

class ImagePickingAdapter(
    private val context: Context,
    private val items: MutableList<FileItem>,
    private val listener: OnImageClickListener
) : RecyclerView.Adapter<ImagePickingAdapter.FileViewHolder>() {
    private val defaultStartEndMargin: Int = context.resources.getDimensionPixelSize(R.dimen.twenty)
    private val defaultTopMargin: Int = context.resources.getDimensionPixelSize(R.dimen.ten)

    class FileViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgPicking)
        val del: ImageView = v.findViewById(R.id.imgDelete)
        val delete: ImageView = v.findViewById(R.id.imgaudiodelete)
        val imgVideoPlay: ImageView = v.findViewById(R.id.imgVideoPlay)
        val imgVideo: ImageView = v.findViewById(R.id.imgVideo)
        val lblTime: TextView = v.findViewById(R.id.lblTime)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_picking_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, pos: Int) {
        val item = items[pos]
        Log.d("isFileType", item.type.toString())
        Log.d("isFilePath", item.path.toString())

        // Layout margins
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.marginStart = defaultStartEndMargin
        layoutParams.marginEnd = defaultStartEndMargin
        layoutParams.topMargin = defaultTopMargin

        holder.itemView.layoutParams = layoutParams

        val filePath = item.path
        val fileUri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") -> Uri.parse(
                filePath
            )

            filePath.startsWith("http://") || filePath.startsWith("https://") -> filePath
            else -> File(filePath)
        }

        val placeholderRes = when (item.type) {
            FileType.PDF -> R.drawable.pdf_icon
            FileType.DOC, FileType.DOCX -> R.drawable.doc_icon
            FileType.PPT -> R.drawable.ppt_icon
            FileType.EXCEL -> R.drawable.excel_icon
            FileType.TXT -> R.drawable.txt_icon
            FileType.IMAGE -> R.drawable.image_placeholder
            FileType.VIDEO -> R.drawable.black
            FileType.AUDIO -> R.drawable.voice
            else -> R.drawable.address_icon
        }

        Glide.with(context)
            .load(fileUri)
            .placeholder(placeholderRes)
            .apply(RequestOptions().dontTransform())
            .error(placeholderRes)
            .into(holder.img)


        holder.del.visibility = if (pos == 0) GONE else VISIBLE
        holder.del.setOnClickListener {
            Log.d("isPosition", pos.toString())
            Constant.Remaining = Constant.Remaining + 1
            items.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, items.size)
        }

        if (item.type.toString() == Constant.VIDEO) {
            holder.imgVideo.visibility = VISIBLE
        } else {
            holder.imgVideo.visibility = GONE
        }
        holder.itemView.setOnClickListener {
            if (pos != 0) {
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
                        Constant.selectedFileIndex = indexInFiltered - 1
                        val intent = Intent(context, FilesViewActivity::class.java)
                        intent.putExtra(Constant.subjectName, "Your Files")
                        context.startActivity(intent)
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
                                "Please download an app to view this file.",
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
            } else {
                listener.onImageClick(pos)
            }
        }
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "*/*"
    }

    override fun getItemCount() = items.size
}

