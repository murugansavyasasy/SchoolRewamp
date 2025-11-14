package com.vs.schoolmessenger.Parent.Assignment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.DataClass.AssignmentData
import com.vs.schoolmessenger.School.NoticeBoard.SchoolNoticeBoardAdapter
import com.vs.schoolmessenger.Utils.Constant

class AssignmentAdapter(
    itemList: MutableList<AssignmentData>,
    private val listener: AssignmentClickListener,
    private val context: Context,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private var fullList: MutableList<AssignmentData> = ArrayList(itemList)
    private var filteredList: MutableList<AssignmentData> = ArrayList(itemList)

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
                .inflate(R.layout.assignment_report_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            filteredList[position].let {
                holder.bind(it, position, this, listener)
            }
        } else if (holder is SchoolNoticeBoardAdapter.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    fun updateList(newList: List<AssignmentData>) {
        fullList.clear()
        fullList.addAll(newList)
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase() ?: ""
                val resultList = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        (it.title?.lowercase()?.contains(query) == true) ||
                                (it.description?.lowercase()?.contains(query) == true) ||
                                (it.subject?.lowercase()?.contains(query) == true)
                    }.toMutableList()
                }
                return FilterResults().apply { values = resultList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = (results?.values as? MutableList<AssignmentData>) ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun removeItemAt(position: Int) {
        if (position in filteredList.indices) {
            val removedNotice = filteredList[position]
            filteredList.removeAt(position)
            fullList = fullList.filterNot { it.id == removedNotice.id }.toMutableList()

            notifyItemRemoved(position)
        }
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDescription: TextView = itemView.findViewById(R.id.lblDescription)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblassigned: TextView = itemView.findViewById(R.id.lblassigned)
        private val lblCategory: TextView = itemView.findViewById(R.id.lblCategory)
        private val lblSubject: TextView = itemView.findViewById(R.id.lblSubject)
        private val lblSubmitted: TextView = itemView.findViewById(R.id.lblSubmitted)
        private val lblNotSubmitted: TextView = itemView.findViewById(R.id.lblNotSubmitted)
        private val lbldeadline: TextView = itemView.findViewById(R.id.lbldeadline)
        private val createddate: TextView = itemView.findViewById(R.id.createddate)
        private val lblSendby: TextView = itemView.findViewById(R.id.lblSendby)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)

        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val lblSubmittedCount: TextView = itemView.findViewById(R.id.lblSubmittedCount)
        private val imgNewImage: ImageView = itemView.findViewById(R.id.imgNewImage)

        private val options: ImageView = itemView.findViewById(R.id.options)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val progressBarAssignment: ProgressBar =
            itemView.findViewById(R.id.progressBarAssignment)
        private val headerrelative_layout: RelativeLayout =
            itemView.findViewById(R.id.headerrelative_layout)

        @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
        fun bind(
            data: AssignmentData,
            position: Int,
            adapter: AssignmentAdapter,
            listener: AssignmentClickListener
        ) {




            lblDescription.text = data.description
            lblTitle.text = data.title
            lblCategory.text = data.category
            lblassigned.text =
                 "${context.getString(R.string.Assigned)} - ${Constant.convertToReadableDate(data.created_date)}"
            createddate.text = Constant.convertToReadableDate(data.created_date)
            lblSubject.text = data.subject
            lbldeadline.text =
                "${context.getString(R.string.Deadline)} - ${Constant.convertToReadableDate(data.end_date)}"
            lblSendby.text = data.created_date

            lblSubmitted.text = "${context.getString(R.string.submitted)} - ${data.submitted_count}"
            lblNotSubmitted.text = "${context.getString(R.string.not_submitted)} - ${data.total_count}"

            lblSubmittedCount.text = data.submitted_count.toString()+"/"+data.total_count.toString()


            val submittedCount = data.submitted_count ?: 0
            val totalCount = data.total_count ?: 1

            progressBarAssignment.max = totalCount
            progressBarAssignment.progress = submittedCount

            val hasFiles = !data.file_path.isNullOrEmpty()

            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
            total_numbers.visibility = View.GONE

            rytList2.setOnClickListener {

             val targetType = when (data.recipient_type.trim()) {
                 "SCHOOL" -> 1
                 "STANDARD" -> 2
                 "SECTION" -> 3
                 "GROUP" -> 4
                 "STUDENT" -> 5
                 "STAFF" -> 6
                 else -> 0
             }

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
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = data.submitted_count,
                    totalCount = data.total_count,
                    target_type = targetType,
                    assignmentid = data.id,
                    created_date = data.created_date,
                    category = data.category,
                    assignmentsubject = data.subject,
                    isParentAssignment = false
                )


                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            headerrelative_layout.setOnClickListener {
                val targetType = when (data.recipient_type.trim()) {
                    "SCHOOL" -> 1
                    "STANDARD" -> 2
                    "SECTION" -> 3
                    "GROUP" -> 4
                    "STUDENT" -> 5
                    "STAFF" -> 6
                    else -> 0
                }

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
                    isCompleted = true,
                    isMenuType = Constant.M_ASSIGNMENT,
                    fileList = convertedList,
                    submittedCount = data.submitted_count,
                    totalCount = data.total_count,
                    target_type = targetType,
                    assignmentid = data.id,
                    created_date = data.created_date,
                    category = data.category,
                    assignmentsubject = data.subject,
                    isParentAssignment = false
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
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

                            val targetType = when (data.recipient_type.trim()) {
                                "SCHOOL" -> 1
                                "STANDARD" -> 2
                                "SECTION" -> 3
                                "GROUP" -> 4
                                "STUDENT" -> 5
                                "STAFF" -> 6
                                else -> 0
                            }

                            val isHomeWorkData = FilePreview(
                                id = data.id,
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
                                totalCount = data.total_count,
                                target_type = targetType,
                                assignmentid = data.id,
                                created_date = data.created_date,
                                category = data.category,
                                assignmentsubject = data.subject,
                                isParentAssignment = false
                            )

                            val intent = Intent(context, ChildHomeWork::class.java)
                            intent.putExtra(Constant.isPreViewData, isHomeWorkData)
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
                    subjectName = data.subject ?: "",
                    fullList = fileList,
                    visibleList = visibleList,
                    context = context,
                    isLoading = Constant.isShimmerViewDisable
                )

                rcyAssignment.adapter = fileAdapter
            }

            lblSubmitted.setOnClickListener { listener.onSubmittedClick(data) }
            lblNotSubmitted.setOnClickListener { listener.onNotSubmittedClick(data) }

            if (data.can_edit && data.can_delete) {

                options.visibility = View.VISIBLE
            } else {

                options.visibility = View.GONE
            }


            options.setOnClickListener {

                listener.onEditAndDeleteClick(data, it, adapterPosition)
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
