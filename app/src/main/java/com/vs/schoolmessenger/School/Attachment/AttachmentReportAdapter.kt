package com.vs.schoolmessenger.School.Attachment

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentReportData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.*
import kotlin.collections.ArrayList

class AttachmentReportAdapter(
    private var attachmentList: List<AttachmentReportData>?,
    private val childClickListener: OnAttachmentReportClickListener,
    private val context: Context,
    var isLoading: Boolean,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {


    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: ArrayList<AttachmentReportData> =
        ArrayList(attachmentList ?: emptyList())

    private var filteredList: List<AttachmentReportData> = attachmentList ?: emptyList()

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
            holder.bind(filteredList, position, childClickListener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }

    fun removeItemAt(position: Int) {
        if (position in filteredList.indices) {
            val itemToRemove = filteredList[position]
            val mutableList = originalList.toMutableList()
            mutableList.remove(itemToRemove)
            originalList = ArrayList(mutableList)
            filter.filter("") // refresh filtered list
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, filteredList.size)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val resultList = if (charString.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.title?.lowercase(Locale.getDefault())?.contains(charString) == true ||
                                it.description?.lowercase(Locale.getDefault())?.contains(charString) == true ||
                                it.sent_by?.lowercase(Locale.getDefault())?.contains(charString) == true
                    }
                }

                return FilterResults().apply {
                    values = resultList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<AttachmentReportData> ?: emptyList()
                notifyDataSetChanged()

                if (filteredList.isEmpty()) {
                    noDataImage?.visibility = View.VISIBLE
                    noDataText?.visibility = View.VISIBLE
                } else {
                    noDataImage?.visibility = View.GONE
                    noDataText?.visibility = View.GONE
                }
            }
        }
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: OnAttachmentReportClickListener
    ) : RecyclerView.ViewHolder(itemView) {

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

            imgReadUnRead.visibility = if (data.is_unread) View.VISIBLE else View.GONE

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
                    imgReadUnRead.visibility = View.GONE
                    listener.onReadStatusClick(item, adapterPosition)
                }
            }

            rytHeader.setOnClickListener {
                if (item[position].is_unread) {
                    item[position].is_unread = false
                    imgReadUnRead.visibility = View.GONE
                    listener.onReadStatusClick(item, adapterPosition)
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