package com.vs.schoolmessenger.Dashboard.School

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
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
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.SchoolList
import com.vs.schoolmessenger.Dashboard.Model.AdItem
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.Parent.AdImageAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.AbsenteesMarking.AttendanceMark
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesReport
import com.vs.schoolmessenger.School.Assignment.Assignment
import com.vs.schoolmessenger.School.Communication.CommunicationSchool
import com.vs.schoolmessenger.School.DailyCollection.DailyCollection
import com.vs.schoolmessenger.School.Event.CreateEvent
import com.vs.schoolmessenger.School.ExamSchedule.Exam
import com.vs.schoolmessenger.School.FeePendingReport.FeePendingReport
import com.vs.schoolmessenger.School.Homework.HomeWork
import com.vs.schoolmessenger.School.ImagePDF.ImagePdf
import com.vs.schoolmessenger.School.ImportantInfo.ImportantInfo
import com.vs.schoolmessenger.School.InteractionWithStudent.InteractionWithStudent
import com.vs.schoolmessenger.School.LeaveRequests.LeaveRequests
import com.vs.schoolmessenger.School.LessonPlan.LessonPlan
import com.vs.schoolmessenger.School.MarkYourAttendance.MarkYourAttendance
import com.vs.schoolmessenger.School.MessageFromManagement.MessageFromManagement
import com.vs.schoolmessenger.School.NoticeBoard.CreateNoticeBoard
import com.vs.schoolmessenger.School.OnlineMeeting.OnlineMeeting
import com.vs.schoolmessenger.School.PTM.PTM
import com.vs.schoolmessenger.School.SchoolNeeds.SchoolNeeds
import com.vs.schoolmessenger.School.SchoolStrength.SchoolStrength
import com.vs.schoolmessenger.School.StaffWiseAttendanceReport.StaffWiseAttendanceReport
import com.vs.schoolmessenger.School.StudentReport.StudentReport
import com.vs.schoolmessenger.School.Video.CreateVideo
import java.util.Timer
import java.util.TimerTask


class SchoolMenuAdapter(
    private var context: Context,
    private var itemList: ArrayList<GridItem>?,
    private val specialImages: List<AdItem>? = null,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_AD = 2

    private var isSeeMore = false
    private var seeMoreMenus = 0

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_SHIMMER
            position == 9 -> TYPE_AD
            else -> TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.shimmer_dashboard_item, parent, false)
                ShimmerViewHolder(view)
            }

            TYPE_AD -> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.ad_item, parent, false)
                AdViewHolder(view, this)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item_card, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                holder.bind(itemList!!, position)
            }

            is AdViewHolder -> {
                if (position == 9) {
                    holder.bind(specialImages!!, context)
                } else {
                    holder.bind(emptyList(), context)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList!!.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: ArrayList<GridItem>, position: Int) {
            imgMenu.setImageResource(data[position].image)
            lblMenuName.text = data[position].title
            rlaMenu.setOnClickListener {

                when (data[position].title) {

                    "Communication" -> {
                        context.startActivity(Intent(context, CommunicationSchool::class.java))
                    }

                    "Image/Pdf" -> {
                        context.startActivity(Intent(context, ImagePdf::class.java))
                    }

                    "Video Upload" -> {
                        context.startActivity(Intent(context, CreateVideo::class.java))
                    }

                    "Notice Board" -> {
                        context.startActivity(Intent(context, CreateNoticeBoard::class.java))
                    }

                    "Leave Requests" -> {
                        context.startActivity(Intent(context, LeaveRequests::class.java))
                    }

                    "Assignment" -> {
                        context.startActivity(Intent(context, SchoolList::class.java))
                    }

                    "Home Work" -> {
                        context.startActivity(Intent(context, HomeWork::class.java))
                    }

                    "Attendance Marking" -> {
                        context.startActivity(Intent(context, AttendanceMark::class.java))
                    }

                    "Message From Management" -> {
                        context.startActivity(Intent(context, MessageFromManagement::class.java))
                    }

                    "Interaction With Student" -> {
                        context.startActivity(Intent(context, InteractionWithStudent::class.java))
                    }

                    "Lesson Plan" -> {
                        context.startActivity(Intent(context, LessonPlan::class.java))
                    }

                    "PTM" -> {
                        context.startActivity(Intent(context, PTM::class.java))
                    }

                    "Event" -> {
                        context.startActivity(Intent(context, CreateEvent::class.java))
                    }

                    "School Needs" -> {
                        context.startActivity(Intent(context, SchoolNeeds::class.java))
                    }

                    "Important Info" -> {
                        context.startActivity(Intent(context, ImportantInfo::class.java))
                    }

                    "Absentees Report" -> {
                        context.startActivity(Intent(context, AbsenteesReport::class.java))
                    }

                    "School Strength" -> {
                        context.startActivity(Intent(context, SchoolStrength::class.java))
                    }

                    "Daily Collection" -> {
                        context.startActivity(Intent(context, DailyCollection::class.java))
                    }

                    "Student Report" -> {
                        context.startActivity(Intent(context, StudentReport::class.java))
                    }

                    "Online Meeting" -> {
                        context.startActivity(Intent(context, OnlineMeeting::class.java))
                    }

                    "Fee Pending Report" -> {
                        context.startActivity(Intent(context, FeePendingReport::class.java))
                    }

                    "Mark Your Attendance" -> {
                        context.startActivity(Intent(context, MarkYourAttendance::class.java))
                    }

                    "Staff Wise Attendance Report" -> {
                        context.startActivity(
                            Intent(
                                context, StaffWiseAttendanceReport::class.java
                            )
                        )
                    }

                    "Schedule Exam/Test" -> {
                        context.startActivity(Intent(context, Exam::class.java))
                    }
                }
            }
        }
    }

    fun toggleMoreItems(lblSeeMore: TextView, rlaMenuExample: LinearLayout) {
        if (isSeeMore) {
            lblSeeMore.text = context.getString(R.string.SeeAll)
            rlaMenuExample.visibility = View.GONE
            val startPosition = itemList!!.size - seeMoreMenus
            itemList!!.subList(startPosition, itemList!!.size).clear()
            notifyItemRangeRemoved(startPosition, seeMoreMenus)
            seeMoreMenus = 0
        } else {
            rlaMenuExample.visibility = View.GONE
            lblSeeMore.text = context.getString(R.string.SeeLess)
            val moreItems = getMoreItems()
            seeMoreMenus = moreItems.size
            val startPosition = itemList!!.size
            itemList!!.addAll(moreItems)
            notifyItemRangeInserted(startPosition, seeMoreMenus)
        }
        isSeeMore = !isSeeMore
    }

    fun toggleMoreItems() {
        seeMoreMenus = 0
        val moreItems = getMenu()
        seeMoreMenus = moreItems.size
        itemList!!.addAll(moreItems)
    }

    class AdViewHolder(itemView: View, private val adapter: SchoolMenuAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewAdImages)
        private val lnrHomeWork: LinearLayout = itemView.findViewById(R.id.lnrHomeWork)
        private val lnrLeaveRequest: LinearLayout = itemView.findViewById(R.id.lnrLeaveRequest)
        private val lnrAssignment: LinearLayout = itemView.findViewById(R.id.lnrAssignment)
        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
        private val dotContainer: LinearLayout = itemView.findViewById(R.id.dotContainer)
        private val rlaMenuExample: LinearLayout = itemView.findViewById(R.id.rlaMenuExample)
        private var isFirstTime = true
        private lateinit var layoutManager: LinearLayoutManager
        private var position: Int = 0
        private val handler = Handler(Looper.getMainLooper())
        private var isTouching = false
        private var timer: Timer? = null
        private var timerTask: TimerTask? = null

        @SuppressLint("ClickableViewAccessibility")
        fun bind(images: List<AdItem>, context: Context) {
            // Initialize layoutManager
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = AdImageAdapter(images)
            rlaMenuExample.visibility = View.GONE

            lblSeeMore.setOnClickListener {
                adapter.toggleMoreItems(lblSeeMore, rlaMenuExample)
            }

            lnrAssignment.setOnClickListener {
                context.startActivity(Intent(context, Assignment::class.java))
            }
            lnrLeaveRequest.setOnClickListener {
                context.startActivity(
                    Intent(
                        context, LeaveRequests::class.java
                    )
                )
            }
            lnrHomeWork.setOnClickListener {
                context.startActivity(
                    Intent(
                        context, com.vs.schoolmessenger.Parent.Homework.HomeWork::class.java
                    )
                )
            }

            runAutoScrollBanner(images)
            if (isFirstTime) {
                isFirstTime = false
                setupDots(images.size, context)
            } else {
                setupDots(images.size + 1, context)
            }

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if (isFirstTime) {
                            isFirstTime = false
                            position =
                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size + 1
                        } else {
                            position =
                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size
                        }
                        updateDots(position)
                    }
                }
            })

            // Delay the auto-scroll for better user experience (start scrolling after 2 seconds)
            handler.postDelayed({ runAutoScrollBanner(images) }, 3000)

            recyclerView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isTouching = true
                        stopAutoScrollBanner()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isTouching = false
                        handler.postDelayed({ if (!isTouching) runAutoScrollBanner(images) }, 500)
                    }
                }
                false
            }

            adapter.toggleMoreItems()
        }

        private fun setupDots(count: Int, context: Context) {
            dotContainer.removeAllViews()
            for (i in 0 until count) {
                val dot = ImageView(context).apply {
                    setImageResource(if (i == position) R.drawable.active_dot else R.drawable.inactive_dot)
                    val params = LinearLayout.LayoutParams(20, 20)
                    params.setMargins(8, 0, 8, 0)
                    layoutParams = params
                }
                dotContainer.addView(dot)
            }
        }

        private fun updateDots(activePosition: Int) {
            for (i in 0 until dotContainer.childCount) {
                val dot = dotContainer.getChildAt(i) as ImageView
                dot.setImageResource(if (i == activePosition) R.drawable.active_dot else R.drawable.inactive_dot)
            }
        }

        private fun stopAutoScrollBanner() {
            timerTask?.cancel()
            timer?.cancel()
            timer = null
            timerTask = null
            position = layoutManager.findFirstCompletelyVisibleItemPosition()
        }

        private fun runAutoScrollBanner(images: List<AdItem>) {
            if (timer == null && timerTask == null) {
                timer = Timer()
                timerTask = object : TimerTask() {
                    override fun run() {
                        handler.post {
                            position++
                            if (isFirstTime) {
                                isFirstTime = false
                                recyclerView.smoothScrollToPosition(position % images.size + 1) // Loop within the list size
                            } else {
                                recyclerView.smoothScrollToPosition(position % images.size) // Loop within the list size
                            }
                        }
                    }
                }
                timer?.schedule(timerTask, 3000, 3000)
            }
        }
    }

    private fun getMoreItems(): ArrayList<GridItem> {
        return arrayListOf(
            GridItem(R.drawable.interact_with_student, "Interaction With Student"),
            GridItem(R.drawable.lesson_plan, "Lesson Plan"),
            GridItem(R.drawable.ptm_school, "PTM"),
            GridItem(R.drawable.event_icon_school, "Event"),
            GridItem(R.drawable.school_needs, "School Needs"),
            GridItem(R.drawable.importent_info, "Important Info"),
            GridItem(R.drawable.absentees_report, "Absentees Report"),
            GridItem(R.drawable.school_strength, "School Strength"),
            GridItem(R.drawable.daily_collection, "Daily Collection"),
            GridItem(R.drawable.student_report, "Student Report"),
            GridItem(R.drawable.online_meeting_icon, "Online Meeting"),
            GridItem(R.drawable.fee_pending_reports, "Fee Pending Report"),
            GridItem(R.drawable.mark_your_attendance, "Mark Your Attendance"),
            GridItem(R.drawable.staff_attendance_report, "Staff Wise Attendance Report")
        )
    }

    private fun getMenu(): ArrayList<GridItem> {
        return arrayListOf(
            GridItem(R.drawable.interact_with_student, "Interaction With Student"),
            GridItem(R.drawable.lesson_plan, "Lesson Plan"),
            GridItem(R.drawable.ptm_school, "PTM")
        )
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}