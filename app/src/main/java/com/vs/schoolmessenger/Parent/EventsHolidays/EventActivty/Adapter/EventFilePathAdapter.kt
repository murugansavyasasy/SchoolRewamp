package com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.Parent.Communication.UnifiedVoiceAdapter
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.FilePath
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class EventFilePathAdapter(

    private var GetFilePathDetailsData: List<FilePath>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var visibleCount = 3

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.attachement_rewamp_recycler)
            UnifiedVoiceAdapter.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attachement_rewamp_recycler, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) {
            20
        } else {
            GetFilePathDetailsData?.size?.coerceAtMost(visibleCount) ?: 0
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            GetFilePathDetailsData?.getOrNull(position)?.let {
                holder.bind(it, position, this)
                val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
                val scale = holder.itemView.context.resources.displayMetrics.density
                val overlapMargin = (10 * scale + 0.5f).toInt()
                layoutParams.marginStart = if (position != 0) -overlapMargin else 0
                holder.itemView.layoutParams = layoutParams
            }
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val DefaultImage: ShapeableImageView = itemView.findViewById(R.id.ImgPDF)

        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            data: FilePath?,
            position: Int,
            adapter: EventFilePathAdapter,
        ) {

            DefaultImage.visibility = View.VISIBLE

            when (data?.type?.uppercase()) {
                Constant.IMAGE -> {
                    Glide.with(context).load(data.url)
                        .placeholder(R.drawable.image_placeholder)
                        .into(DefaultImage)
                }

                Constant.AUDIO -> {
                    Glide.with(context).load(R.drawable.voice).into(DefaultImage)
                    DefaultImage.setImageResource(R.drawable.voice)
                }

                Constant.PDF, Constant.DOC, Constant.DOCX, Constant.TXT, Constant.PPT, Constant.PPTX, Constant.EXCEL, Constant.VIDEO -> {
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
                Constant.VIDEO -> R.drawable.video_play
                else -> R.drawable.doc_icon
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
