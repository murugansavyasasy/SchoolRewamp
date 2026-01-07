package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.ImageSliderAdapter
import com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter.ChildHomeWork
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.FilePreview
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.GetFilePathDetails
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.AvgPerformanceModel.AvgStudentSubmission
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.ItemStudentlistReccleBinding

class StudentListAdapter(
    private val items: List<AvgStudentSubmission>, private val context: Context
) : RecyclerView.Adapter<StudentListAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemStudentlistReccleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentlistReccleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            if (item.subject.isNullOrBlank()) {
                txtTitle.text = context.getString(R.string.no_subject_available)
            } else {
                txtTitle.text = item.subject
            }
            txtSubTitle.text = item.activity_type
            txtDescription.text = item.title
            txtsubdesc.text = item.description
            if (item.created_on.isNullOrBlank()) {
                txtDate.text = context.getString(R.string.no_data_available)
            } else {
                txtDate.text = Constant.convertDateTimeFormat(item.created_on)
            }

            txtSubmitted.text = item.submitted_average + " " + context.getString(R.string.submitted)

            imgIcon.setImageResource(
                when (item.activity_type) {
                    Constant.Listening -> R.drawable.headphonesvgformat
                    Constant.Speaking -> R.drawable.micsvgformatstyle
                    Constant.Reading -> R.drawable.booksvg_formatstyle
                    Constant.Writing -> R.drawable.pensvgformatstyle
                    else -> R.drawable.questionmark
                }
            )

            val hasFiles = !item.file_path.isNullOrEmpty()

            rytList2.visibility = if (hasFiles) View.GONE else View.GONE
            totalNumbers.visibility = View.GONE

            rytList2.setOnClickListener { openChildHomeWork(item) }
            headerrelativeLayout.setOnClickListener { openChildHomeWork(item) }

            rcyAssignment.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null && e.action == MotionEvent.ACTION_UP) {
                        rv.getChildAdapterPosition(child)
                        openChildHomeWork(item)
                    }
                    return false
                }
            })

            if (hasFiles) {
                val fileList = item.file_path!!
                val totalFiles = fileList.size
                val visibleList = if (totalFiles > 3) fileList.subList(0, 3) else fileList

                if (totalFiles > 3) {
                    totalNumbers.text = "+${totalFiles - 3}"
                    totalNumbers.visibility = View.VISIBLE
                } else {
                    totalNumbers.visibility = View.GONE
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

    override fun getItemCount(): Int = items.size

    private fun openChildHomeWork(item: AvgStudentSubmission) {
        val convertedList = item.file_path?.map {
            GetFilePathDetails(type = it.type, url = it.url)
        } ?: emptyList()

        val isHomeWorkData = FilePreview(
            id = item.id,
            title = item.title,
            description = item.description,
            subjectName = item.subject,
            sentBy = "",
            thumbnail = "",
            isUnread = true,
            isCompleted = true,
            isMenuType = Constant.M_LSRW,
            fileList = convertedList,
            submittedCount = 0,
            totalCount = 0,
            assignmentid = item.activity_type,
            created_date = item.created_on,
            category = "",
            assignmentsubject = "",
            isParentAssignment = false
        )

        val intent = Intent(context, ChildHomeWork::class.java)
        intent.putExtra(Constant.isPreViewData, isHomeWorkData)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }
}

