package com.vs.schoolmessenger.School.QuizExam.Adapter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FileType
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import java.io.File

class QuestionAttachmentAdapter(
    private val context: Context,
    private val list: MutableList<FilePath>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<QuestionAttachmentAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img = v.findViewById<ImageView>(R.id.imgAttachment)
        val remove = v.findViewById<ImageView>(R.id.imgRemove)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        return VH(
            LayoutInflater.from(context).inflate(R.layout.item_question_attachment, p, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = list[pos]


// ---------- LOAD IMAGE / PLACEHOLDER ----------
        val filePath = item.url

        val fileUri = when {
            filePath.startsWith("content://") || filePath.startsWith("file://") ->
                Uri.parse(filePath)

            filePath.startsWith("http://") || filePath.startsWith("https://") ->
                filePath

            else -> File(filePath)
        }

        val placeholderRes = when (item.type) {
            FileType.PDF.toString() -> R.drawable.pdf_icon
            FileType.DOC.toString(), FileType.DOCX.toString() -> R.drawable.doc_icon
            FileType.PPT.toString() -> R.drawable.ppt_icon
            FileType.EXCEL.toString() -> R.drawable.excel_icon
            FileType.TXT.toString() -> R.drawable.txt_icon
            FileType.IMAGE.toString() -> R.drawable.image_placeholder
            FileType.VIDEO.toString() -> R.drawable.video_play
            else -> R.drawable.wrong_file
        }

        Glide.with(context)
            .load(fileUri)
            .placeholder(placeholderRes)
            .error(placeholderRes)
            .into(h.img)

        h.remove.setOnClickListener {
            onRemove(pos)
        }

        h.img.setOnClickListener {

            if (!item.url.contains("amazonaws.")) {

                if (item.type == Constant.IMAGE || item.type == Constant.VIDEO) {

                    val filteredFiles = list.filter {
                        it.type == Constant.IMAGE || it.type == Constant.VIDEO
                    }

                    Constant.commonFileList = filteredFiles.map {
                        CommonFileData(
                            type = it.type.toString(),
                            path = it.url
                        )
                    }.toMutableList()

                    val clickedPath = item.url
                    val indexInFiltered = filteredFiles
                        .indexOfFirst { it.url == clickedPath }
                        .let { if (it >= 0) it else 0 }

                    Constant.selectedFileIndex = indexInFiltered

                    val intent = Intent(context, FilesViewActivity::class.java)
                    intent.putExtra(Constant.subjectName, "Your Files")
                    context.startActivity(intent)

                } else {
                    val uri = if (item.url.startsWith("content://")) {
                        Uri.parse(item.url)
                    } else {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            File(item.url)
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
                Constant.commonFileList = list.map {
                    CommonFileData(
                        type = it.type.toString(),
                        path = it.url
                    )
                }.toMutableList()

                Constant.selectedFileIndex = pos

                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, "Your Files")
                context.startActivity(intent)
            }
        }

    }
    private fun getMimeTypeFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver
        return contentResolver.getType(uri) ?: "*/*"
    }
}
