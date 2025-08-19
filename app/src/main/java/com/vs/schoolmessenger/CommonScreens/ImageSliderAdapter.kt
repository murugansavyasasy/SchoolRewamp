package com.vs.schoolmessenger.CommonScreens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.FilePath
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class ImageSliderAdapter(
    private var subjectName: String?,
    private var fullList: List<FilePath>,
    private var visibleList: List<FilePath>,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int = if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.homework_img_pdf_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.homework_img_pdf_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int = if (isLoading) 3 else visibleList.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(fullList, visibleList[position], position, subjectName ?: "")
            val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            val scale = holder.itemView.context.resources.displayMetrics.density
            val overlapMargin = (10 * scale + 0.5f).toInt()
            layoutParams.marginStart = if (position != 0) -overlapMargin else 0
            holder.itemView.layoutParams = layoutParams
        }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val DefaultImage: ImageView = itemView.findViewById(R.id.ImgPDF)
        private val fileItem: RelativeLayout = itemView.findViewById(R.id.fileItem)

        private var triedRawLoad = false

        fun bind(
            fullList: List<FilePath>,
            data: FilePath,
            position: Int,
            subjectName: String
        ) {
            DefaultImage.visibility = View.VISIBLE

            when (data.type.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context).load(data.url)
                        .placeholder(R.drawable.image_placeholder)
                        .into(DefaultImage)
                }

                Constant.AUDIO -> {
                    Glide.with(context).load(R.drawable.voice).into(DefaultImage)
                    DefaultImage.setImageResource(R.drawable.voice)
                }

                Constant.PDF, Constant.DOC, Constant.DOCX, Constant.TXT, Constant.PPT, Constant.PPTX, Constant.EXCEL -> {
                    DefaultImage.setImageResource(getIconForType(data.type))
                }
            }

        }

        private fun getIconForType(type: String): Int {
            return when (type.uppercase()) {
                Constant.PDF -> R.drawable.hw_pdf_img
                Constant.DOC, Constant.DOCX -> R.drawable.microsoft_word_img
                Constant.TXT -> R.drawable.txt_file_img
                Constant.PPT, Constant.PPTX -> R.drawable.ppt_icon
                Constant.EXCEL -> R.drawable.excel_icon
                else -> R.drawable.doc_icon
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
