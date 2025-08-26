package com.vs.schoolmessenger.School.LSRW

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
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Model.lsrwskilldata
import com.vs.schoolmessenger.School.LSRW.SubmissionStudentListModel.StudentSubmissionLsrw
import com.vs.schoolmessenger.Utils.Constant

class SubmittedStudentlistAdapter (
    private var itemList: List<StudentSubmissionLsrw>,
    private val context: Context
) : RecyclerView.Adapter<SubmittedStudentlistAdapter.HeaderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.submitted_studentlist_detail, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = itemList.size


    fun updateList(newList: List<StudentSubmissionLsrw>) {
        itemList = newList
        notifyDataSetChanged()
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblStudentName: TextView = itemView.findViewById(R.id.lblStudentName)
        private val sectionlabel: TextView = itemView.findViewById(R.id.sectionlabel)
        private val submittedDate: TextView = itemView.findViewById(R.id.submittedDate)
        private val statuslabel: TextView = itemView.findViewById(R.id.statuslabel)

        private val rcyAssignment: RecyclerView = itemView.findViewById(R.id.rcyAssignment)

        private val rytList2: RelativeLayout = itemView.findViewById(R.id.rytList2)
        private val total_numbers: TextView = itemView.findViewById(R.id.total_numbers)
        private val cancelimage: ImageView = itemView.findViewById(R.id.cancelimage)
        private val headerrelative_layout: RelativeLayout = itemView.findViewById(R.id.rlarelativelayout)
        private val statusButton: LinearLayout = itemView.findViewById(R.id.statusButton)

        fun bind(item: StudentSubmissionLsrw) {
            lblStudentName.text = item.student_name
            sectionlabel.text = item.standard +" - "+ item.section
            submittedDate.text = item.submitted_date


            if (item.submit_status == "SUBMITTED") {
                statuslabel.text = "Submitted"
                cancelimage.setBackgroundResource(R.drawable.correcticonsvg)
                statuslabel.setTextColor(
                    ContextCompat.getColor(context, R.color.clr_green)
                )
                statusButton.setBackgroundResource(R.drawable.completed_button_bg)
            } else {
                statuslabel.text = "Pending"
                cancelimage.setBackgroundResource(R.drawable.downloadsvgformat)
                statuslabel.setTextColor("#9e6e40".toColorInt())

                statusButton.setBackgroundResource(R.drawable.pending_button_bg)
            }





            val hasFiles = !item.file_path.isNullOrEmpty()


            rytList2.visibility = if (hasFiles) View.VISIBLE else View.GONE
            total_numbers.visibility = View.GONE

            rytList2.setOnClickListener {
                val convertedList = item.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = item.id,
                    title = "",
                    description = "",
                    subjectName = "",
                    sentBy = "",
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_SCHOOL_NEEDS,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = "",
                    created_date = "",
                    category = "",
                    assignmentsubject = ""
                )

                val intent = Intent(context, ChildHomeWork::class.java)
                intent.putExtra("isPreViewData", isHomeWorkData)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }

            headerrelative_layout.setOnClickListener {
                val convertedList = item.file_path.map {
                    GetFilePathDetails(
                        type = it.type,
                        url = it.url,
                    )
                }
                val isHomeWorkData = FilePreview(
                    id = item.id,
                    title = "",
                    description = "",
                    subjectName = "",
                    sentBy = "",
                    thumbnail = item.thumbnail,
                    isUnread = true,
                    isCompleted = true,
                    isMenuType = Constant.M_SCHOOL_NEEDS,
                    fileList = convertedList,
                    submittedCount = 0,
                    totalCount = 0,
                    assignmentid = "",
                    created_date = "",
                    category = "",
                    assignmentsubject = "",
                    isParentAssignment = false
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
                            val convertedList = item.file_path.map {
                                GetFilePathDetails(
                                    type = it.type,
                                    url = it.url,
                                )
                            }
                            val isHomeWorkData = FilePreview(
                                id = item.id,
                                title = "",
                                description = "",
                                subjectName = "",
                                sentBy = "",
                                thumbnail = item.thumbnail,
                                isUnread = true,
                                isCompleted = true,
                                isMenuType = Constant.M_SCHOOL_NEEDS,
                                fileList = convertedList,
                                submittedCount = 0,
                                totalCount = 0,
                                assignmentid = "",
                                created_date = "",
                                category = "",
                                assignmentsubject = "",
                                isParentAssignment = false
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
                val fileList = item.file_path!!
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
}
