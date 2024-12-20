package com.vs.schoolmessenger.Dashboard.School

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.SchoolList
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesStudentMark
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.Assignment.Assignment
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.LessonPlan.LessonPlan
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeeting
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.School.Video.CreateVideo

class SchoolMenuAdapter(
    private var context: Context, private val itemList: List<GridItem>?,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.shimmer_dashboard_item, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.menu_item_card, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position])
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: GridItem) {
            imgMenu.setImageResource(data.image)
            lblMenuName.text = data.title

            rlaMenu.setOnClickListener {
                when (data.title) {
                    "Video Upload" -> {
                        context.startActivity(Intent(context, CreateVideo::class.java))
                    }
                    "Communication" -> {
                        context.startActivity(Intent(context, CommunicationSchool::class.java))
                    }
                    "Attendance Marking" -> {
                        context.startActivity(Intent(context, AttendanceMark::class.java))
                    }
                    "Event" -> {
                        context.startActivity(Intent(context, CreateEvent::class.java))
                    }

                    "Notice Board" -> {
                        context.startActivity(Intent(context, CreateNoticeBoard::class.java))
                    }

                    "Assignment" -> {
                        context.startActivity(Intent(context, SchoolList::class.java))
                    }

                    "Student Report" -> {
                        context.startActivity(Intent(context, StudentReport::class.java))
                    }

                    "School Strength" -> {
                        context.startActivity(Intent(context, SchoolStrength::class.java))
                    }
                    "Lesson Plan" -> {
                        context.startActivity(Intent(context, LessonPlan::class.java))
                    }

                    "Online Meeting" -> {
                        context.startActivity(Intent(context, OnlineMeeting::class.java))
                    }




                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout = itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer() // Start shimmer effect
        }
    }
}
