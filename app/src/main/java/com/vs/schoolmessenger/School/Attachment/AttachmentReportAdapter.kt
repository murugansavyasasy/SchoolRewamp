
package com.vs.schoolmessenger.School.Attachment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.MessageFromManagement.Model.GetMessagesStaffData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale

class AttachmentReportAdapter(
    private var attachmentList: List<AttachmentDataReport>?,
    private val childClickListener: OnAttachmentReportClickListener,
    private val context: Context,
    var isLoading: Boolean,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {


    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList: ArrayList<AttachmentDataReport> =
        ArrayList(attachmentList ?: emptyList())

    private var filteredList: List<AttachmentDataReport> = attachmentList ?: emptyList()

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
                val charString =
                    constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val resultList = if (charString.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.title?.lowercase(Locale.getDefault())?.contains(charString) == true ||
                                it.description?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true ||
                                it.sent_by?.lowercase(Locale.getDefault())
                                    ?.contains(charString) == true
                    }
                }

                return FilterResults().apply {
                    values = resultList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? List<AttachmentDataReport> ?: emptyList()
                notifyDataSetChanged()

                val isEmpty = filteredList.isEmpty()
                Log.d("NoData", if (isEmpty) "No data" else "Data")

                noDataImage?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                noDataText?.visibility = if (isEmpty) View.VISIBLE else View.GONE

                // Notify activity/fragment about filter state change
                childClickListener.onFilterEmpty(isEmpty)
            }
        }
    }
    fun AppendData(newList: List<AttachmentDataReport>) {
        val oldSize = filteredList!!.size
        filteredList = filteredList!!.toMutableList().apply { addAll(newList) }
        originalList=ArrayList(filteredList)
        Log.d("FinalList",originalList.size.toString())
        Log.d("FinalList",filteredList.size.toString())
        notifyItemRangeInserted(oldSize, newList.size)
    }

    fun getCurrentListSize(): Int {
        return filteredList!!.size
    }

    fun getCurrentList(): List<AttachmentDataReport> {
        return filteredList
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
            item: List<AttachmentDataReport>,
            position: Int,
            listener: OnAttachmentReportClickListener,
            adapter: AttachmentReportAdapter,
        ) {
            val data = item[position]
            lblDate.text = "${context.getString(R.string.posted_on)} : ${Constant.convertDateAndTimeFormat(data.date)}"
            lblTitle.text = data.title
            lblPostedBy.text = "${context.getString(R.string.posted_by)} : ${data.sent_by}"
            lblDescription.text = data.description
            lblDescription.maxLines = 3
            lblDescription.ellipsize = TextUtils.TruncateAt.END
            lblSeeMore.visibility = View.GONE

            var isExpanded = false

            lblDescription.maxLines = Integer.MAX_VALUE
            lblDescription.ellipsize = null
            lblDescription.text = data.description

            lblDescription.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    lblDescription.viewTreeObserver.removeOnPreDrawListener(this)

                    if (lblDescription.lineCount > 3) {
                        lblDescription.maxLines = 3
                        lblDescription.ellipsize = TextUtils.TruncateAt.END
                        lblSeeMore.visibility = View.VISIBLE
                    } else {
                        lblSeeMore.visibility = View.GONE
                    }
                    return true
                }
            })


            lblSeeMore.setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) {
                    lblDescription.maxLines = Int.MAX_VALUE
                    lblDescription.ellipsize = null
                    lblSeeMore.text = context.getString(R.string.See_Less_1)
                } else {
                    lblDescription.maxLines = 3
                    lblDescription.ellipsize = TextUtils.TruncateAt.END
                    lblSeeMore.text = context.getString(R.string.see_more)
                }
            }


            if (data.file_path.isNotEmpty()) {
                rcyFile.visibility = View.VISIBLE
            } else {
                rcyFile.visibility = View.GONE
            }

            imgEditAndDelete.visibility =
                if (data.can_delete && data.can_edit) View.VISIBLE else View.GONE
            imgReadUnRead.visibility = if (data.is_unread) View.VISIBLE else View.GONE

            imgEditAndDelete.setOnClickListener {
                listener.onItemClick(item, it, adapterPosition)
            }

            val markAsRead = {
                if (data.is_unread) {
                    data.is_unread = false
                    imgReadUnRead.visibility = View.GONE
                    listener.onReadStatusClick(item, adapterPosition)
                }
            }

            rcyFile.setOnClickListener { markAsRead() }
            rytHeader.setOnClickListener {
                markAsRead()

                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    subjectName = "",
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    created_date = data.date,
                    target_type = data.target_type.toIntOrNull(),
                    isCompleted = true,
                    isMenuType = Constant.M_ATTACHMENTS,
                    fileList = convertedList,
                    submittedCount = 0,
                    assignmentid = "",
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = false
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)


            }

            val attachmentAdapter = AttachmentFileView(data.file_path, context, "")
            rcyFile.layoutManager = GridLayoutManager(context, 3)
            rcyFile.isNestedScrollingEnabled = false
            rcyFile.adapter = attachmentAdapter


            rcyFile.addOnItemTouchListener(
                object : RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        if (child != null && e.action == MotionEvent.ACTION_UP) {
                            rv.getChildAdapterPosition(child)
                            Log.d("RecyclerTouch", "Clicked position: $position")
                            if (data.is_unread) {
                                data.is_unread = false
                                imgReadUnRead.visibility = View.GONE
                                listener.onReadStatusClick(item, adapterPosition)
                            }
                        }
                        return false
                    }
                }
            )
        }
    }



    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)?.startShimmer()
        }
    }
}