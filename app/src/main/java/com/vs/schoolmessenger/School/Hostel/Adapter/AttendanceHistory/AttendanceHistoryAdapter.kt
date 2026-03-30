package com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getRoomData
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getSchoolSessionData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceHistoryAdapter(
    private var itemList: List<getSchoolSessionData>?,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var expandedPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attendance_history_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<getSchoolSessionData>) {
        itemList = newList
        notifyDataSetChanged()
    }



    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val consTotalStudentDetails: ConstraintLayout = itemView.findViewById(R.id.consTotalStudentDetails)
        private val consPresentDetails: ConstraintLayout = itemView.findViewById(R.id.consPresentDetails)
        private val consAbsentDetails: ConstraintLayout = itemView.findViewById(R.id.consAbsentDetails)
        private val lblNoStudnets: TextView = itemView.findViewById(R.id.lblNoStudnets)
        private val lblAttendanceDate: TextView = itemView.findViewById(R.id.lblAttendanceDate)
        private val lblSessionNo: TextView = itemView.findViewById(R.id.lblSessionNo)
        private val lblAttendancePercentage: TextView = itemView.findViewById(R.id.lblAttendancePercentage)
        private val lblTotalStudentCount: TextView = itemView.findViewById(R.id.lblTotalStudentCount)
        private val lblPresentCount: TextView = itemView.findViewById(R.id.lblPresentCount)
        private val lblAbsentCount: TextView = itemView.findViewById(R.id.lblAbsentCount)
        private val lblViewStudents: MaterialButton = itemView.findViewById(R.id.lblViewStudents)
        private val View3: View = itemView.findViewById(R.id.View3)
        private val rcStudentsDetails: RecyclerView = itemView.findViewById(R.id.rcStudentsDetails)

        fun bind(data: getSchoolSessionData, position: Int) {

            lblSessionNo.text="${position+1}"

            val isExpanded = position == expandedPosition

            lblAttendanceDate.text = data.session_name

            val totalStudent = data.students.size
            val count = if (totalStudent == 1)
                context.getString(R.string.student)
            else
                context.getString(R.string.students)

            lblNoStudnets.text = "$totalStudent $count"
            lblTotalStudentCount.text = totalStudent.toString()
            lblPresentCount.text = data.present_count
            lblAbsentCount.text = data.absent_count

            val present = data.present_count.toIntOrNull() ?: 0

            val percentage = if (totalStudent > 0) {
                ((present.toFloat() / totalStudent.toFloat()) * 100).toInt()
            } else 0

            lblAttendancePercentage.text = "$percentage%"

            // Expand / Collapse visibility
            rcStudentsDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
            View3.visibility = if (isExpanded) View.VISIBLE else View.GONE
            lblViewStudents.text = if (isExpanded) context.getString(R.string.hide_students) else context.getString(R.string.view_students)

            lblViewStudents.setIconResource(
                if (isExpanded) R.drawable.ic_up_arrow
                else R.drawable.ic_down_arrow
            )

            // Load inner adapter ONLY when expanded
            if (isExpanded && data.students.isNotEmpty()) {
                rcStudentsDetails.layoutManager = LinearLayoutManager(context)
                rcStudentsDetails.isNestedScrollingEnabled = false
                rcStudentsDetails.adapter =
                    AttendanceStudentHistoryWise(data.students, context, false)
            }

            lblViewStudents.setBackgroundTintList(
                ContextCompat.getColorStateList(context, R.color.very_gray_white_1)
            )

            lblViewStudents.setOnClickListener {

                val previous = expandedPosition

                expandedPosition = if (isExpanded) {
                    -1 // collapse
                } else {
                    position // expand new
                }

                notifyItemChanged(previous)
                notifyItemChanged(position)
            }

            setDrawableBackgroundColor(consTotalStudentDetails, R.color.light_blue_13)
            setDrawableBackgroundColor(consPresentDetails, R.color.light_green_12)
            setDrawableBackgroundColor(consAbsentDetails, R.color.light_red_9)
        }

        fun setDrawableBackgroundColor(view: View, colorRes: Int) {
            val bgDrawable = view.background as? GradientDrawable
            bgDrawable?.setColor(ContextCompat.getColor(view.context, colorRes))
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}