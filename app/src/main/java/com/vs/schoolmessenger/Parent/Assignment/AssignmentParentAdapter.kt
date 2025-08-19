package com.vs.schoolmessenger.Parent.Assignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Assignment.Model.ParentAssignmentData
import com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission.MyAssignmentSubmit
import com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission.Mysubmission
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant

class AssignmentParentAdapter(
    var itemList: MutableList<ParentAssignmentData>,
    private val listener: AssignmentClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<ParentAssignmentData> = itemList ?: listOf()
    private var filteredList: List<ParentAssignmentData> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_parent_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position, this, listener)
            }
        } else if (holder is SchoolNoticeBoardAdapter.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    fun removeItemAt(position: Int) {
        if (position in filteredList.indices) {
            val removedNotice = filteredList[position]
            filteredList = filteredList.toMutableList().apply {
                removeAt(position)
            }
            fullList = fullList.filterNot { it.id == removedNotice.id }
            notifyItemRemoved(position)
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblCategory: TextView = itemView.findViewById(R.id.lblCategory)


        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)

        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)

        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val lblassigned: TextView = itemView.findViewById(R.id.lblassigned)
        private val lbldeadline: TextView = itemView.findViewById(R.id.lbldeadline)
        private val lblSubmitted: TextView = itemView.findViewById(R.id.lblSubmitted)
        private val lblNotSubmitted: TextView = itemView.findViewById(R.id.lblNotSubmitted)

        private val headerrelative_layout: RelativeLayout =
            itemView.findViewById(R.id.headerrelative_layout)

        @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
        fun bind(
            data: ParentAssignmentData,
            position: Int,
            adapter: AssignmentParentAdapter,
            listener: AssignmentClickListener
        ) {

            lblDescription.text = data.description
            lblTitle.text = data.title
            lblCategory.text = data.category
            lblassigned.text = data.subject
            lbldeadline.text = data.end_date


            val hasIframe = !data.iframe.isNullOrEmpty()
            val hasFiles = !data.file_path.isNullOrEmpty()

            video_player.visibility = if (hasIframe) View.VISIBLE else View.GONE
            rcyAssignment.visibility = if (hasIframe) View.GONE else View.VISIBLE
            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
            total_numbers.visibility = View.GONE

            rytList2.setOnClickListener {
                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = "",
                    title = data.title,
                    description = data.description,
                    subjectName = "",
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = data.submitted_count,
                    assignmentid = data.id,
                    category = data.category,
                    assignmentsubject = data.subject
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            headerrelative_layout.setOnClickListener {
                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = "",
                    title = data.title,
                    description = data.description,
                    subjectName = "",
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = data.submitted_count,
                    assignmentid = data.id,
                    category = data.category,
                    assignmentsubject = data.subject,
                    isParentAssignment = true
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rcyAssignment.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null && e.action == MotionEvent.ACTION_UP) {
                        rv.getChildAdapterPosition(child)
                        val convertedList = data.file_path.map {
                            GetFilePathDetails(
                                type = it.type,
                                url = it.url,
                            )
                        }
                        val isHomeWorkData = FilePreview(
                            id = "",
                            title = data.title,
                            description = data.description,
                            subjectName = "",
                            sentBy = "",
                            thumbnail = data.thumbnail,
                            isUnread = true,
                            isCompleted = true,
                            isMenuType = Constant.M_ASSIGNMENT,
                            fileList = convertedList,
                            submittedCount = data.submitted_count,
                            assignmentid = data.id,
                            category = data.category,
                            assignmentsubject = data.subject,
                            isParentAssignment = true
                        )

                        val intent = Intent(context, ChildHomeWork::class.java)
                        intent.putExtra("isPreViewData", isHomeWorkData)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    }
                    return false
                }
            })

            lblSubmitted.setOnClickListener {
                val intent = Intent(context, Mysubmission::class.java)
                intent.putExtra("assignment_id", data.header_id)
                intent.putExtra("title", data.title)
                intent.putExtra("subject", data.subject)
                context.startActivity(intent)
            }
            lblNotSubmitted.setOnClickListener {
                val intent = Intent(context, MyAssignmentSubmit::class.java)
                intent.putExtra("assignment_id", data.header_id)
                intent.putExtra("title", data.title)
                intent.putExtra("subject", data.subject)
                context.startActivity(intent)
            }


            if (hasFiles) {
                val fileList = data.file_path!!
                val totalFiles = fileList.size
                val visibleList = if (totalFiles > 3) fileList.subList(0, 3) else fileList

                if (totalFiles > 3) {
                    total_numbers.text = "+${totalFiles - 3}"
                    total_numbers.visibility = View.VISIBLE
                } else {
                    total_numbers.visibility = View.GONE
                }

                rcyAssignment.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                val fileAdapter = ImageSliderAdapter(
                    subjectName = data.subject ?: "",
                    fullList = fileList,
                    visibleList = visibleList,
                    context = context,
                    isLoading = Constant.isShimmerViewDisable
                )

                rcyAssignment.adapter = fileAdapter
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
