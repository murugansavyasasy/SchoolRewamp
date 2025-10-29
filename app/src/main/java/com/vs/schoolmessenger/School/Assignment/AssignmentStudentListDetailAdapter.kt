package com.vs.schoolmessenger.School.Assignment

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Assignment.Model.SubmissionDetail
import com.vs.schoolmessenger.School.Event.Model.SchoolEventItem
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AssignmentStudentListDetailAdapter(
    private var itemList: List<SubmissionDetail>?,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(
                parent, R.layout.assignment_adapter_student_detailreport
            )
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.assignment_adapter_student_detailreport, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let {
                holder.bind(it, position, this)
            }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList?.size ?: 0
    }

    fun updateList(newData: List<SubmissionDetail>) {
        itemList = newData
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View, private val context: Context
    ) : RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val rytList: LinearLayout = itemView.findViewById(R.id.rytList)
        private val video_player: ImageView = itemView.findViewById(R.id.video_player)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)


        fun bind(
            data: SubmissionDetail, position: Int, adapter: AssignmentStudentListDetailAdapter
        ) {
            lblStudentName.text = data.description
            sectionlabel.text = data.submitted_on
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
                    title = "",
                    description = data.description,
                    subjectName = "",
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
                intent.putExtra(Constant.isPreViewData, isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            rytList.setOnClickListener {
                val convertedList = data.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = data.id,
                    title ="",
                    description = data.description,
                    subjectName = "",
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
                            val isHomeWorkData = FilePreview(
                                id = data.id,
                                title = "",
                                description = data.description,
                                subjectName = "",
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
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}
