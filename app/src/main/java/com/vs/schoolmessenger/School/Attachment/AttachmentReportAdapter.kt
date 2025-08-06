package com.vs.schoolmessenger.School.Attachment

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData
import com.vs.schoolmessenger.School.Homework.HomeWorkReportModel.HomeWorkReport
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttachmentReportAdapter(
    private var attachmentList: List<AttachmentReportData>?,
    private val childClickListener: OnAttachmentReportClickListener,
    private val context: Context,
    var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private var originalList: ArrayList<AttachmentReportData> = ArrayList(attachmentList ?: emptyList())

    private val TYPE_DATA = 1
    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attachment_report_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attachment_report_item, parent, false)
            DataViewHolder(view, context, childClickListener)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!isLoading && holder is DataViewHolder) {
               holder.bind(attachmentList!!, position, childClickListener , this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else attachmentList!!.size
    }

    fun removeItemAt(position: Int) {
        if (position in attachmentList!!.indices) {
            val mutableList = attachmentList!!.toMutableList()
            val removedItem = mutableList.removeAt(position)
            attachmentList = mutableList
            originalList = originalList.filter { it != removedItem } as ArrayList<AttachmentReportData>
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, attachmentList!!.size)
        }
    }


    class DataViewHolder(
        itemView: View, private val context: Context, private val listener: OnAttachmentReportClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblPostedBy: TextView = itemView.findViewById(R.id.lblPostedBy)
        private val rcyFile: RecyclerView = itemView.findViewById(R.id.rcyFile)
        private val imgEditAndDelete: ImageView = itemView.findViewById(R.id.imgEditAndDelete)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            item: List<AttachmentReportData>,
            position: Int,
            listener: OnAttachmentReportClickListener,
            adapter: AttachmentReportAdapter,
        ) {
            lblDate.text= item[position].date
            lblTitle.text=item[position].title
            lblDescription.text=item[position].description
            lblPostedBy.text = "Posted By : " + item[position].sent_by

            if (item[position].file_path.isNotEmpty()) {
                rcyFile.visibility = View.VISIBLE
            } else {
                rcyFile.visibility = View.GONE
            }

            imgEditAndDelete.setOnClickListener {
                listener.onItemClick(item, it, adapterPosition)
            }

            val isAttachmentFileView =
                AttachmentFileView(item[position].file_path, context,"")
            rcyFile.layoutManager = GridLayoutManager(context, 3)
            rcyFile.isNestedScrollingEnabled = false
            rcyFile.adapter = isAttachmentFileView
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}