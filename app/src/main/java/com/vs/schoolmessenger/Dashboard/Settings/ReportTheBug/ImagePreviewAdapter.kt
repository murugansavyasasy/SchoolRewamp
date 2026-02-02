package com.vs.schoolmessenger.Dashboard.Settings.ReportTheBug

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem
import com.vs.schoolmessenger.Utils.FileType
import java.io.File

class ImagePreviewAdapter(
    private val imagePathList: MutableList<FileItem>,
    private val context: Context,
    private val listener: ImagePreviewRemoveListener
) : BaseAdapter() {

    override fun getCount(): Int = imagePathList.size
    override fun getItem(position: Int): Any = imagePathList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.image_preview, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = imagePathList[position]
        holder.imgCancel.tag = position
        holder.imgCancel.setOnClickListener {
            val pos = it.tag as Int   // Correct position every time
            listener.remove(pos)
        }

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

        holder.imgGallery.setOnClickListener {
            if (item.type == FileType.IMAGE || item.type == FileType.VIDEO || item.type == FileType.AUDIO) {

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
        }

        Glide.with(context)
            .load(fileUri)
            .placeholder(placeholderRes)
            .apply(RequestOptions().dontTransform())
            .dontAnimate()  // Skip fade-in for snappier lists
            .priority(Priority.HIGH)  // Prioritize over other loads
            .error(placeholderRes)
            .into(holder.imgGallery)

        if (item.type.toString() == Constant.VIDEO) {
            holder.imgVideo.visibility = VISIBLE
        } else {
            holder.imgVideo.visibility = GONE
        }

        return view
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "*/*"
    }


    class ViewHolder(view: View) {
        val imgGallery: ImageView = view.findViewById(R.id.imgGallery)
        val imgVideo: ImageView = view.findViewById(R.id.imgVideo)
        val imgCancel: ImageView = view.findViewById(R.id.imgCancle)
    }
}

