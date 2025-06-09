package com.vs.schoolmessenger.Parent.Attachment.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentClickListener
import com.vs.schoolmessenger.Parent.Attachment.Model.AttachmentData

import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttachmentAdapter(
    private var attachmentList: List<AttachmentData>?,
    private val listener: AttachmentClickListener,
    private val context: Context,
    var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.noticeboard_report_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.noticeboard_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
            holder.bind(attachmentList!![position], listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else attachmentList?.size ?: 0
    }

    fun updateList(newList: List<AttachmentData>) {
        this.attachmentList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDateImage: TextView = itemView.findViewById(R.id.lblDateImage)
        private val lblTitleImage: TextView = itemView.findViewById(R.id.lblTitleImage)
        private val lblContentImage: TextView = itemView.findViewById(R.id.lblContentImage)
        private val rcyImgPdf: RecyclerView = itemView.findViewById(R.id.rcyImgPDF)
        private val rytPin: FrameLayout = itemView.findViewById(R.id.rytPin)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)

        fun bind(item: AttachmentData, listener: AttachmentClickListener) {
            lblTitleImage.text = item.title
            lblContentImage.text = item.description
            lblDateImage.text = Constant.convertDateAndTimeFormat(item.date)

            rytPin.visibility = View.GONE
            imgNewImage.visibility = if (item.is_unread) View.VISIBLE else View.GONE

            if (item.file_path.isNotEmpty()) {
                rcyImgPdf.visibility = View.VISIBLE
                rcyImgPdf.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                rcyImgPdf.adapter = AttachmentFilePathAdapter(item.file_path, context, Constant.isShimmerViewDisable)
            } else {
                rcyImgPdf.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listener.onItemClick(item, this)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}
