package com.vs.schoolmessenger.School.QuizExam.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.CommonScreens.FileType
import com.vs.schoolmessenger.Parent.Assignment.Model.FilePath
import com.vs.schoolmessenger.R

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

        if (item.type == FileType.IMAGE.toString()) {
            Glide.with(context).load(item.url).into(h.img)
        } else {
            val placeholderRes = when (item.type) {
                FileType.PDF.toString() -> R.drawable.pdf_icon
                FileType.DOC.toString(), FileType.DOCX.toString() -> R.drawable.doc_icon
                FileType.PPT.toString() -> R.drawable.ppt_icon
                FileType.EXCEL.toString() -> R.drawable.excel_icon
                FileType.TXT.toString() -> R.drawable.txt_icon
                FileType.VIDEO.toString() -> R.drawable.video_play
                else -> R.drawable.wrong_file
            }
            Glide.with(context).load(placeholderRes).into(h.img)
        }
        h.remove.setOnClickListener {
            onRemove(pos)
        }
    }
}
