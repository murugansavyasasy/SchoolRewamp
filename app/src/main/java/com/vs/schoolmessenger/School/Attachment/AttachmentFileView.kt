package com.vs.schoolmessenger.School.Attachment

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.CommonFileData
import com.vs.schoolmessenger.CommonScreens.FilesViewActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentFilePath
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttachmentFileView(
    private var fileList: List<AttachmentFilePath>?,
    private val context: Context,
    private val isSubjectName: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    var isLoading = false

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attachment_image_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attachment_image_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else fileList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && fileList != null) {
            holder.bind(fileList!!, position, context, isSubjectName)
        }
    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAttachment: ImageView = itemView.findViewById(R.id.imgAttachment)

        fun bind(
            fullList: List<AttachmentFilePath>,
            position: Int,
            context: Context,
            isSubjectName: String
        ) {
            val data = fullList[position]

            // Load image or placeholder based on type
            when (data.type.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context)
                        .load(data.url)
                        .placeholder(R.drawable.image_placeholder)
                        .into(imgAttachment)
                }
                Constant.PDF -> imgAttachment.setBackgroundResource(R.drawable.hw_pdf_img)
                Constant.DOC, Constant.DOCX -> imgAttachment.setBackgroundResource(R.drawable.microsoft_word_img)
                Constant.TXT -> imgAttachment.setBackgroundResource(R.drawable.txt_file_img)
                Constant.PPT, Constant.PPTX -> imgAttachment.setBackgroundResource(R.drawable.ppt_icon)
                Constant.EXCEL -> imgAttachment.setBackgroundResource(R.drawable.excel_icon)
                Constant.VIDEO -> imgAttachment.setBackgroundResource(R.drawable.video_type_icon)
                else -> imgAttachment.setBackgroundResource(R.drawable.image_pdf_icon)
            }

            // Click event to open FilesViewActivity
            itemView.setOnClickListener {
                val commonList = fullList.map {
                    CommonFileData(
                        type = it.type,
                        path = it.url
                    )
                }.toMutableList()

                Constant.commonFileList = commonList
                Constant.selectedFileIndex = position

                val intent = Intent(context, FilesViewActivity::class.java)
                intent.putExtra(Constant.subjectName, isSubjectName)
                context.startActivity(intent)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}
