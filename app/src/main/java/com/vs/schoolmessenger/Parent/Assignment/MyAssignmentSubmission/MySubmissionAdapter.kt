package com.vs.schoolmessenger.Parent.Assignment.MyAssignmentSubmission

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
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
import com.vs.schoolmessenger.Parent.Assignment.AssignmentClickListener
import com.vs.schoolmessenger.Parent.Assignment.MySubmissionModel.SubmittedAssignment
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant
import java.text.SimpleDateFormat
import java.util.Locale

class MySubmissionAdapter(
    var itemList: MutableList<SubmittedAssignment>,
    private val listener: AssignmentClickListener,
    private val context: Context,
    private val isLoading: Boolean,
    private val title: String?,
    private val subject: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: List<SubmittedAssignment> = itemList ?: listOf()
    private var filteredList: List<SubmittedAssignment> = itemList ?: listOf()

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
                .inflate(R.layout.mysubmission_assignment_detail, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList.getOrNull(position)?.let {
                holder.bind(it, position, this, listener, title, subject)
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

    fun updateList(newList: List<SubmittedAssignment>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }



    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblDescription1: TextView = itemView.findViewById(R.id.lblDescription1)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)

        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val submitteddetails: TextView = itemView.findViewById(R.id.submitteddetails)
        private val datevalue: TextView = itemView.findViewById(R.id.datevalue)
        private val headerrelative_layout: RelativeLayout =
            itemView.findViewById(R.id.headerrelative_layout)

        @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
        fun bind(
            data: SubmittedAssignment,
            position: Int,
            adapter: MySubmissionAdapter,
            listener: AssignmentClickListener,
            title: String?,
            subject: String?
        ) {
            lblDescription1.text = data.description
            lblTitle.text = title
            lblDescription.text = subject


            try {
                val apiFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
                val parsedDate = apiFormat.parse(data.submitted_on)

                if (parsedDate != null) {

                    submitteddetails.text = "submitted: " + DateUtils.getRelativeTimeSpanString(
                        parsedDate.time,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )


                    val shortDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    datevalue.text = shortDateFormat.format(parsedDate)
                } else {
                    submitteddetails.text = data.submitted_on
                    datevalue.text = data.submitted_on
                }
            } catch (e: Exception) {
                e.printStackTrace()
                submitteddetails.text = data.submitted_on
                datevalue.text = data.submitted_on
            }


            val hasFiles = !data.file_path.isNullOrEmpty()

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
                    id = data.id,
                    title = title.toString(),
                    description = data.description,
                    subjectName = subject,
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = 0,
                    assignmentid = data.id,
                    category = "",
                    assignmentsubject = ""
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
                    id = data.id,
                    title = title.toString(),
                    description = data.description,
                    subjectName = subject,
                    sentBy = "",
                    thumbnail = data.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = 0,
                    assignmentid = data.id,
                    category = "",
                    assignmentsubject = ""
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rcyAssignment.addOnItemTouchListener(
                object : RecyclerView.SimpleOnItemTouchListener() {
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
                                title = title.toString(),
                                description = data.description,
                                subjectName = subject,
                                sentBy = "",
                                thumbnail = data.thumbnail,
                                isUnread = true,
                                isCompleted = true,
                                isMenuType = Constant.M_ASSIGNMENT,
                                fileList = convertedList,
                                submittedCount = 0,
                                assignmentid = data.id,
                                category = "",
                                assignmentsubject = ""
                            )

                            val intent = Intent(context, ChildHomeWork::class.java)
                            intent.putExtra("isPreViewData", isHomeWorkData)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            context.startActivity(intent)
                        }
                        return false
                    }
                }
            )




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
                    subjectName = "",
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
