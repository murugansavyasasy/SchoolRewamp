package com.vs.schoolmessenger.School.Attachment

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttachmentReportAdapter(
    private var attachmentList: List<AttachmentReportData>?,
    private val childClickListener: OnAttachmentReportClickListener,
    private val context: Context,
    var isLoading: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private var originalList: ArrayList<AttachmentReportData> =
        ArrayList(attachmentList ?: emptyList())

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
            holder.bind(attachmentList!!, position, childClickListener, this)
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
            originalList =
                originalList.filter { it != removedItem } as ArrayList<AttachmentReportData>
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, attachmentList!!.size)
        }
    }


    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: OnAttachmentReportClickListener
    ) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblPostedBy: TextView = itemView.findViewById(R.id.lblPostedBy)
        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        private val rytHeader: RelativeLayout = itemView.findViewById(R.id.rytHeader)
        private val rcyFile: RecyclerView = itemView.findViewById(R.id.rcyFile)
        private val imgEditAndDelete: ImageView = itemView.findViewById(R.id.imgEditAndDelete)
        private val imgReadUnRead: ImageView = itemView.findViewById(R.id.imgReadUnRead)


        @SuppressLint("ClickableViewAccessibility")
        fun bind(
            item: List<AttachmentReportData>,
            position: Int,
            listener: OnAttachmentReportClickListener,
            adapter: AttachmentReportAdapter,
        ) {
            val data = item[position]

            lblDate.text = Constant.convertDateAndTimeFormat(data.date)
            lblTitle.text = data.title
            lblDescription.text = data.description
            lblPostedBy.text = "Posted By : ${data.sent_by}"

            if (data.file_path.isNotEmpty()) {
                rcyFile.visibility = View.VISIBLE
            } else {
                rcyFile.visibility = View.GONE
            }

            imgEditAndDelete.visibility =
                if (data.can_delete && data.can_edit) View.VISIBLE else View.GONE

            lblDescription.maxLines = 3
            lblDescription.ellipsize = TextUtils.TruncateAt.END
            lblSeeMore.text = "See More"
            lblSeeMore.visibility = View.GONE

            var isExpanded = false

            lblDescription.post {
                if (lblDescription.lineCount > 3) {
                    lblSeeMore.visibility = View.VISIBLE
                    lblDescription.maxLines = 3
                    lblDescription.ellipsize = TextUtils.TruncateAt.END
                    lblSeeMore.text = "See More"
                } else {
                    lblSeeMore.visibility = View.GONE
                }
            }
            if (item[position].is_unread) {
                imgReadUnRead.visibility = View.VISIBLE
            } else {
                imgReadUnRead.visibility = View.GONE
            }


            lblSeeMore.setOnClickListener {
                if (isExpanded) {
                    lblDescription.maxLines = 3
                    lblDescription.ellipsize = TextUtils.TruncateAt.END
                    lblSeeMore.text = "See More"
                } else {
                    lblDescription.maxLines = Int.MAX_VALUE
                    lblDescription.ellipsize = null
                    lblSeeMore.text = "See Less"
                }
                isExpanded = !isExpanded
            }

            imgEditAndDelete.setOnClickListener {
                listener.onItemClick(item, it, adapterPosition)
            }

            rcyFile.setOnClickListener {
                if (item[position].is_unread) {
                    item[position].is_unread = false
                    imgReadUnRead.visibility= View.GONE
                    listener.onReadStatusClick(item,adapterPosition)
                }
            }

            rytHeader.setOnClickListener {
                if (item[position].is_unread) {
                    item[position].is_unread = false
                    imgReadUnRead.visibility= View.GONE
                    listener.onReadStatusClick(item,adapterPosition)
                }
            }


            val attachmentAdapter = AttachmentFileView(data.file_path, context, "")
            rcyFile.layoutManager = GridLayoutManager(context, 3)
            rcyFile.isNestedScrollingEnabled = false
            rcyFile.adapter = attachmentAdapter
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}