package com.vs.schoolmessenger.Parent.Attachment.Adapter

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
import com.vs.schoolmessenger.School.Attachment.AttachmentFileView
import com.vs.schoolmessenger.School.Attachment.DataClass.AttachmentDataReport
import com.vs.schoolmessenger.School.Attachment.OnAttachmentReportClickListener
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Locale

class AttachmentAdapter(
    private val attachmentList: MutableList<AttachmentDataReport>,
    private val childClickListener: OnAttachmentReportClickListener,
    private val context: Context,
    var isLoading: Boolean,
    private val noDataImage: ImageView? = null,
    private val noDataText: TextView? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var originalList = ArrayList(attachmentList ?: emptyList())
    private var filteredList = ArrayList(attachmentList ?: emptyList())


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else filteredList.size
    }

    fun updateFilteredList(newList: List<AttachmentDataReport>) {
        isLoading = false

        filteredList.clear()
        filteredList.addAll(newList)

        originalList.clear()
        originalList.addAll(newList)

        notifyDataSetChanged()
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
        if (holder is DataViewHolder && position < filteredList.size) {

            holder.headerLayout.setBackgroundResource(R.color.white)

            if (!isLoading) {
                holder.bind(filteredList, position, childClickListener, this)
            }
        }
    }


    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase(Locale.getDefault()) ?: ""

                val result = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.title?.lowercase()
                            ?.contains(query) == true || it.description?.lowercase()
                            ?.contains(query) == true || it.sent_by?.lowercase()
                            ?.contains(query) == true
                    }
                }

                return FilterResults().apply { values = result }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                filteredList.addAll(results?.values as? List<AttachmentDataReport> ?: emptyList())

                notifyDataSetChanged()

                val isEmpty = filteredList.isEmpty()
                noDataImage?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                noDataText?.visibility = if (isEmpty) View.VISIBLE else View.GONE
                childClickListener.onFilterEmpty(isEmpty)
            }
        }
    }

    fun getCurrentListSize(): Int = filteredList.size
    fun getCurrentList(): List<AttachmentDataReport> = filteredList


    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val listener: OnAttachmentReportClickListener
    ) : RecyclerView.ViewHolder(itemView) {


        val headerLayout: RelativeLayout =
            itemView.findViewById(R.id.rytHeader)

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
            adapter: AttachmentAdapter,
        ) {

            if (position >= item.size) return

            val data = item[position]

            lblDate.text =
                "${context.getString(R.string.posted_on)} - ${Constant.convertToReadableDate(data.date)}"

            lblTitle.text = data.title
            lblPostedBy.text = "${context.getString(R.string.posted_by)} - ${data.sent_by}"

            lblDescription.text = data.description
            lblDescription.maxLines = 3
            lblDescription.ellipsize = TextUtils.TruncateAt.END
            lblSeeMore.visibility = View.GONE

            lblDescription.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    lblDescription.viewTreeObserver.removeOnPreDrawListener(this)
                    if (lblDescription.lineCount > 3) {
                        lblSeeMore.visibility = View.VISIBLE
                    }
                    return true
                }
            })

            var isExpanded = false

            lblSeeMore.setOnClickListener {
                isExpanded = !isExpanded
                lblDescription.maxLines = if (isExpanded) Int.MAX_VALUE else 3
                lblDescription.ellipsize = if (isExpanded) null else TextUtils.TruncateAt.END
                lblSeeMore.text =
                    context.getString(if (isExpanded) R.string.See_Less_1 else R.string.see_more)
            }

            if (data.file_path.isNotEmpty()) {
                rcyFile.visibility = View.VISIBLE
                rcyFile.layoutManager = GridLayoutManager(context, 3)

                rcyFile.adapter = AttachmentFileView(
                    fileList = data.file_path,
                    context = context,
                    isSubjectName = "",
                    parentDate = data.date,
                    onItemClick = { clickedDate ->
                        Constant.isVideoPostedDate = clickedDate
                        Log.d("ATTACH_DATE", clickedDate)
                    }
                )

                rcyFile.isNestedScrollingEnabled = false
            } else {
                rcyFile.visibility = View.GONE
            }

            imgEditAndDelete.visibility =
                if (data.can_edit && data.can_delete) View.VISIBLE else View.GONE

            imgReadUnRead.visibility = if (data.is_unread) View.VISIBLE else View.GONE

            imgEditAndDelete.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onItemClick(item, it, pos)
                }
            }

            val markAsRead = {
                if (data.is_unread) {
                    data.is_unread = false
                    imgReadUnRead.visibility = View.GONE
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onReadStatusClick(item, pos)
                    }
                }
            }


            rytHeader.setOnClickListener {
                markAsRead()
                Constant.isVideoPostedDate = data.date
                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }

                val previewData = FilePreview(
                    id = data.id,
                    title = data.title,
                    description = data.description,
                    subjectName = "",
                    sentBy = data.sent_by,
                    thumbnail = data.thumbnail,
                    isUnread = false,
                    created_date = data.date,
                    target_type = 0,
                    isCompleted = true,
                    isMenuType = Constant.M_ATTACHMENTS,
                    fileList = convertedList,
                    submittedCount = 0,
                    assignmentid = "",
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = true
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, previewData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rcyFile.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(
                    rv: RecyclerView, e: MotionEvent
                ): Boolean {
                    if (e.action == MotionEvent.ACTION_UP) {
                        markAsRead()
                    }
                    return false
                }
            })
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<ShimmerFrameLayout>(
                R.id.shimmer_view_container
            )?.startShimmer()
        }
    }
}
