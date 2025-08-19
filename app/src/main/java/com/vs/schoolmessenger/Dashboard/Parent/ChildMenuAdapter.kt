package com.vs.schoolmessenger.Dashboard.Parent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuCountDetail
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil


class ChildMenuAdapter(
    private var context: Context,
    private var listener: MenuClickListener,
    private var itemList: List<MenuDetail>?,
    private var itemCountList: ArrayList<MenuCountDetail>?,
    private var isAdItem: List<AdItem>?,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_AD = 2

    override fun getItemViewType(position: Int): Int {
        val showAd = !isLoading && isAdItem?.isNotEmpty() == true && (itemList?.size ?: 0) > 9
        return when {
            isLoading -> TYPE_SHIMMER
            showAd && position == 9 -> TYPE_AD
            else -> TYPE_DATA
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SHIMMER -> {
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.dashboard_app_item)
                ShimmerViewHolder(shimmerView)
            }

//            TYPE_AD -> {
//                val view =
//                    LayoutInflater.from(parent.context).inflate(R.layout.ad_item, parent, false)
//                AdViewHolder(view, this)
//            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.dashboard_app_item, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    fun updateList(newList: List<MenuDetail>) {
        itemList = emptyList()
        itemList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val showAd = isAdItem?.isNotEmpty() == true && (itemList?.size ?: 0) > 9

        when (holder) {
            is DataViewHolder -> {
                val actualPosition = if (showAd && position > 9) position - 1 else position
                itemList?.getOrNull(actualPosition)?.let { menuDetail ->
                    holder.bind(menuDetail, actualPosition, listener,itemCountList)
                }
            }

            is ShimmerViewHolder -> holder.startShimmer()

            //is AdViewHolder -> holder.bind(isAdItem ?: emptyList(), context)
        }
    }


    override fun getItemCount(): Int {
        if (isLoading) return 20
        val baseSize = itemList?.size ?: 0
        val showAd = isAdItem?.isNotEmpty() == true && baseSize > 9
        return if (showAd) baseSize + 1 else baseSize
    }


    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.itemIcon)
        private val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)
        private val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        private val rlaMenu: CardView = itemView.findViewById(R.id.header)
        private val imgReadCount: View = itemView.findViewById(R.id.imgReadCount)

        fun bind(
            data: MenuDetail,
            position: Int,
            listener: MenuClickListener,
            itemCountList: ArrayList<MenuCountDetail>?
        ) {
            itemTitle.text = data.name

            if (itemCountList!![position].unread_count != 0) {
                imgReadCount.visibility = View.VISIBLE
            } else {
                imgReadCount.visibility = View.GONE
            }

            when (data.id) {
                Constant.M_COMMUNICATION -> {
                    imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
                }

                Constant.M_ASSIGNMENT -> {
                    imgMenu.setImageResource(R.drawable.assignment_icon_school)
                }

                Constant.M_HOMEWORK -> {
                    imgMenu.setImageResource(R.drawable.home_work_icon_school)
                }

                Constant.M_ATTENDANCE_MARKING -> {
                    imgMenu.setImageResource(R.drawable.attendance_marking)
                }

                Constant.M_ABSENTEES_REPORT -> {
                    imgMenu.setImageResource(R.drawable.absentees_report)
                }

                Constant.M_SCHOOL_STRENGTH -> {
                    imgMenu.setImageResource(R.drawable.school_strength)
                }

                Constant.M_NOTICEBOARD -> {
                    imgMenu.setImageResource(R.drawable.noticeboard_icon)
                }

                Constant.M_SCHOOL_CLASS_EVENTS -> {
                    imgMenu.setImageResource(R.drawable.graduationevent)
                }

                Constant.M_SCHEDULE_EXAM_TEST -> {
                    imgMenu.setImageResource(R.drawable.schedule_exam_icon)
                }

                Constant.M_MESSAGES_FROM_MANAGEMENT -> {
                    imgMenu.setImageResource(R.drawable.message_f_management)
                }

                Constant.M_FINANCE -> {
                    imgMenu.setImageResource(R.drawable.finance_icon)
                }


                Constant.M_ATTACHMENTS -> {
                    imgMenu.setImageResource(R.drawable.attachement_icon)
                }

                Constant.M_ONLINE_MEETING -> {
                    imgMenu.setImageResource(R.drawable.online_meeting_icon)
                }

                Constant.M_DAILY_COLLECTION -> {
                    imgMenu.setImageResource(R.drawable.collect)
                }

                Constant.M_STUDENT_REPORT -> {
                    imgMenu.setImageResource(R.drawable.student_report)
                }

                Constant.M_LESSON_PLAN -> {
                    imgMenu.setImageResource(R.drawable.lessonplanimage)
                }

                Constant.M_FEEDBACK -> {
                    imgMenu.setImageResource(R.drawable.fee_pending_reports)
                }

                Constant.M_VERY_IMPORTANT_INFO -> {
                    imgMenu.setImageResource(R.drawable.very_important_icon)
                }

                Constant.M_MARK_YOUR_ATTENDANCE -> {
                    imgMenu.setImageResource(R.drawable.attendanceimage)
                }

                Constant.M_STAFF_WISE_ATTENDANCE_REPORT -> {
                    imgMenu.setImageResource(R.drawable.staff_attendance_report)
                }

                Constant.M_LEAVE_REQUEST -> {
                    imgMenu.setImageResource(R.drawable.leave_request_icon_school)
                }

                Constant.M_PTM -> {
                    imgMenu.setImageResource(R.drawable.ptm_school)
                }

                Constant.M_FEE_PENDING_REPORT -> {
                    imgMenu.setImageResource(R.drawable.fee_pending_reports)
                }

                Constant.M_SCHOOL_NEEDS -> {
                    imgMenu.setImageResource(R.drawable.school_needs)
                }
            }

            rlaMenu.setOnClickListener {
                Constant.isSchoolMenuName=data.name
                Constant.SELECTED_SCHOOL_MENU = data.id
                listener.onClick(data)
            }
        }
    }

    class AdViewHolder(itemView: View, private val adapter: ChildMenuAdapter) :
        RecyclerView.ViewHolder(itemView) {
//        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewAdImages)
//        private val rlaMenuExample: LinearLayout = itemView.findViewById(R.id.rlaMenuExample)
//        private lateinit var layoutManager: LinearLayoutManager
//
//        fun bind(isAds: List<AdItem>, context: Context) {
//            layoutManager =
//                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
//            recyclerView.layoutManager = layoutManager
//            recyclerView.adapter = AdImageAdapter(isAds)
//            rlaMenuExample.visibility = View.GONE
//        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}

























//class ChildMenuAdapter(
//    private var context: Context,
//    private var listener: MenuClickListener,
//    private var isMenuDetails: List<MenuDetail>?,
//    private val specialImages: List<AdItem>? = null,
//    private val isLoading: Boolean
//) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private val TYPE_SHIMMER = 0
//    private val TYPE_DATA = 1
//    private val TYPE_AD = 2
//
//    override fun getItemViewType(position: Int): Int {
//        val hasAd = specialImages?.isNotEmpty() == true && isMenuDetails?.size ?: 0 > 9
//        return when {
//            isLoading -> TYPE_SHIMMER
//            hasAd && position == 9 -> TYPE_AD
//            else -> TYPE_DATA
//        }
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        return when (viewType) {
//            TYPE_SHIMMER -> {
//                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.menu_item_card)
//                ShimmerViewHolder(
//                    shimmerView
//                )
//            }
//
//            TYPE_AD -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.ad_item, parent, false)
//                AdViewHolder(view, this)
//            }
//
//            else -> {
//                val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.menu_item_card, parent, false)
//                DataViewHolder(view, context)
//            }
//        }
//    }
//
//    fun updateList(newList: List<MenuDetail>) {
//        isMenuDetails = emptyList()
//        isMenuDetails = newList
//        notifyDataSetChanged()
//    }
//
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        val showAd = specialImages?.isNotEmpty() == true && isMenuDetails?.size ?: 0 > 9
//
//        when (holder) {
//            is DataViewHolder -> {
//                val adjustedPosition = if (showAd && position > 9) position - 1 else position
//                isMenuDetails?.getOrNull(adjustedPosition)?.let { menuDetail ->
//                    holder.bind(menuDetail, adjustedPosition, listener)
//                }
//            }
//
//            is ShimmerViewHolder -> holder.startShimmer()
//
//            is AdViewHolder -> {
//                holder.bind(specialImages ?: emptyList(), context)
//            }
//        }
//    }
//
//
//    override fun getItemCount(): Int {
//        if (isLoading) return 20
//
//        val baseSize = isMenuDetails?.size ?: 0
//        val showAd = specialImages?.isNotEmpty() == true && baseSize > 9
//        return if (showAd) baseSize + 1 else baseSize
//    }
//
//
//    class DataViewHolder(
//        itemView: View,
//        private val context: Context
//    ) :
//        RecyclerView.ViewHolder(itemView) {
//        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
//        private val imgReadCount: View = itemView.findViewById(R.id.imgReadCount)
//        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
//        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)
//
//        fun bind(data: MenuDetail, position: Int, listener: MenuClickListener) {
//            lblMenuName.text = data.name
//
//            if (data.unreadCount >= 1) {
//                imgReadCount.visibility = View.VISIBLE
//            } else {
//                imgReadCount.visibility = View.GONE
//            }
//
//            when (data.id) {
//
//                Constant.M_COMMUNICATION -> {
//                    imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
//                }
//
//                Constant.M_ATTACHMENTS -> {
//                    imgMenu.setImageResource(R.drawable.attachement_icon)
//                }
//
//                Constant.M_HOMEWORK -> {
//                    imgMenu.setImageResource(R.drawable.home_work_icon_school)
//                }
//
//                Constant.M_EXAM -> {
//                    imgMenu.setImageResource(R.drawable.schedule_exam_icon)
//                }
//
//                Constant.M_NOTICEBOARD -> {
//                    imgMenu.setImageResource(R.drawable.noticeboard_icon)
//                }
//
//                Constant.M_PARENT_CLASS_EVENTS -> {
//                    imgMenu.setImageResource(R.drawable.school_or_class_events_icon)
//                }
//
//                Constant.M_ATTENDANCE_REPORT -> {
//                    imgMenu.setImageResource(R.drawable.attendance_report_icon)
//                }
//
//                Constant.M_PARENT_LEAVE_REQUEST -> {
//                    imgMenu.setImageResource(R.drawable.leave_request_icon_school)
//                }
//
//                Constant.M_FEE_DETAILS -> {
//                    imgMenu.setImageResource(R.drawable.fee_details)
//                }
//
//                Constant.M_INTERACTION_WITH_STAFF -> {
//                        imgMenu.setImageResource(R.drawable.interact_with_student)
//                }
//
//
//                Constant.M_ASSIGNMENT -> {
//                    imgMenu.setImageResource(R.drawable.assignment_icon_school)
//                }
//
//                Constant.M_SCHOOL_CLASS_EVENTS -> {
//                    imgMenu.setImageResource(R.drawable.school_or_class_events_icon)
//                }
//
//                Constant.M_ONLINE_MEETING -> {
//                    imgMenu.setImageResource(R.drawable.online_meeting_icon)
//                }
//
//                Constant.M_QUIZ_EXAM -> {
//                    imgMenu.setImageResource(R.drawable.quiz_icon)
//                }
//
//                Constant.M_LSRW -> {
//                    imgMenu.setImageResource(R.drawable.lsrw_icon)
//                }
//
//                Constant.M_CLASS_TIME_TABLE -> {
//                    imgMenu.setImageResource(R.drawable.timetable_icon)
//                }
//
//                Constant.M_CERTIFICATE_REQUEST -> {
//                    imgMenu.setImageResource(R.drawable.certificate_request)
//                }
//
//                Constant.M_COUPON_PACKET -> {
//                    imgMenu.setImageResource(R.drawable.certificate_request)
//                }
//            }
//
//            rlaMenu.setOnClickListener {
//                Constant.isParentMenuName=data.name
//                Constant.SELECTED_PARENT_MENU = data.id
//                listener.onClick(data)
//            }
//        }
//    }
//
//    class AdViewHolder(itemView: View, private val adapter: ChildMenuAdapter) :
//        RecyclerView.ViewHolder(itemView) {
//        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewAdImages)
//        private val lnrHomeWork: LinearLayout = itemView.findViewById(R.id.lnrHomeWork)
//        private val lnrLeaveRequest: LinearLayout = itemView.findViewById(R.id.lnrLeaveRequest)
//        private val lnrAssignment: LinearLayout = itemView.findViewById(R.id.lnrAssignment)
//        private val lblSeeMore: TextView = itemView.findViewById(R.id.lblSeeMore)
//        private val dotContainer: LinearLayout = itemView.findViewById(R.id.dotContainer)
//        private val rlaMenuExample: LinearLayout = itemView.findViewById(R.id.rlaMenuExample)
//        private var isFirstTime = true
//        private lateinit var layoutManager: LinearLayoutManager
//        private var position: Int = 0
//        private val handler = Handler(Looper.getMainLooper())
//        private var isTouching = false
//        private var timer: Timer? = null
//        private var timerTask: TimerTask? = null
//
//        @SuppressLint("ClickableViewAccessibility")
//        fun bind(images: List<AdItem>, context: Context) {
//            // Initialize layoutManager
//            layoutManager =
//                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
//            recyclerView.layoutManager = layoutManager
//            recyclerView.adapter = AdImageAdapter(images)
////            lblSeeMore.setOnClickListener {
////                adapter.toggleMoreItems(lblSeeMore, rlaMenuExample)
////            }
//
////            lnrAssignment.setOnClickListener {
////                context.startActivity(Intent(context, Assignment::class.java))
////            }
////            lnrLeaveRequest.setOnClickListener {
////                context.startActivity(
////                    Intent(
////                        context,
////                        LeaveRequest::class.java
////                    )
////                )
////            }
////            lnrHomeWork.setOnClickListener {
////                context.startActivity(Intent(context, HomeWork::class.java))
////            }
//
////            runAutoScrollBanner(images)
////            if (isFirstTime) {
////                isFirstTime = false
////                setupDots(images.size, context)
////            } else {
////                setupDots(images.size + 1, context)
////            }
//
////            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
////                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
////                    super.onScrollStateChanged(recyclerView, newState)
////                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
////                        if (isFirstTime) {
////                            isFirstTime = false
////                            position =
////                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size + 1
////                        } else {
////                            position =
////                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size
////                        }
////                        updateDots(position)
////                    }
////                }
////            })
//
//            // Delay the auto-scroll for better user experience (start scrolling after 2 seconds)
////            handler.postDelayed({ runAutoScrollBanner(images) }, 3000)
////
////            recyclerView.setOnTouchListener { _, event ->
////                when (event.action) {
////                    MotionEvent.ACTION_DOWN -> {
////                        isTouching = true
////                        stopAutoScrollBanner()
////                    }
////
////                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
////                        isTouching = false
////                        handler.postDelayed({ if (!isTouching) runAutoScrollBanner(images) }, 500)
////                    }
////                }
////                false
////            }
//        }
//
////        private fun setupDots(count: Int, context: Context) {
////            dotContainer.removeAllViews()
////            for (i in 0 until count) {
////                val dot = ImageView(context).apply {
////                    setImageResource(if (i == position) R.drawable.active_dot else R.drawable.inactive_dot)
////                    val params = LinearLayout.LayoutParams(20, 20)
////                    params.setMargins(8, 0, 8, 0)
////                    layoutParams = params
////                }
////                dotContainer.addView(dot)
////            }
////        }
//
////        private fun updateDots(activePosition: Int) {
////            for (i in 0 until dotContainer.childCount) {
////                val dot = dotContainer.getChildAt(i) as ImageView
////                dot.setImageResource(if (i == activePosition) R.drawable.active_dot else R.drawable.inactive_dot)
////            }
////        }
////
////        private fun stopAutoScrollBanner() {
////            timerTask?.cancel()
////            timer?.cancel()
////            timer = null
////            timerTask = null
////            position = layoutManager.findFirstCompletelyVisibleItemPosition()
////        }
//
////        private fun runAutoScrollBanner(images: List<AdItem>) {
////            if (timer == null && timerTask == null) {
////                timer = Timer()
////                timerTask = object : TimerTask() {
////                    override fun run() {
////                        handler.post {
////                            position++
////                            if (isFirstTime) {
////                                isFirstTime = false
////                                recyclerView.smoothScrollToPosition(position % images.size + 1) // Loop within the list size
////                            } else {
////                                recyclerView.smoothScrollToPosition(position % images.size) // Loop within the list size
////                            }
////                        }
////                    }
////                }
////                timer?.schedule(timerTask, 3000, 3000)
////            }
////        }
//    }
//
//    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        fun startShimmer() {
//            ShimmerUtil.startShimmer(itemView)
//        }
//    }
//}