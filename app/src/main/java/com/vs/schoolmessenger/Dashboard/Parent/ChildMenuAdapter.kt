package com.vs.schoolmessenger.Dashboard.Parent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.Parent.Assignment.Assignment
import com.vs.schoolmessenger.Parent.Homework.HomeWork
import com.vs.schoolmessenger.Parent.RequestLeave.LeaveRequest
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Timer
import java.util.TimerTask

class ChildMenuAdapter(
    private var context: Context,
    private var listener: MenuClickListener,
    private var isMenuDetails: List<MenuDetail>?,
    private val specialImages: List<AdItem>? = null,
    private val isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private val TYPE_AD = 2
    private val items = mutableListOf<MenuDetail>()

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
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.menu_item_card)
                ShimmerViewHolder(
                    shimmerView
                )
            }

            TYPE_AD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.ad_item, parent, false)
                AdViewHolder(view, this)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_item_card, parent, false)
                DataViewHolder(view, context)
            }
        }
    }

    fun updateList(newList: List<MenuDetail>) {
        items.clear()
        items.addAll(newList) // use a mutable list inside adapter
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                // holder.bind(isMenuDetails!!, position)
                isMenuDetails?.get(position)?.let { menuDetail ->
                    holder.bind(menuDetail, position, listener)
                }
            }

            is  ShimmerViewHolder -> {
                holder.startShimmer()
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
        else isMenuDetails!!.size
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val imgMenu: ImageView = itemView.findViewById(R.id.imgMenu)
        private val lblMenuName: TextView = itemView.findViewById(R.id.lblMenuName)
        private val rlaMenu: RelativeLayout = itemView.findViewById(R.id.rlaMenu)

        fun bind(data: MenuDetail, position: Int, listener: MenuClickListener) {
            lblMenuName.text = data.name

            when (data.id) {

                Constant.M_COMMUNICATION -> {
                    imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
                }

                Constant.M_HOMEWORK -> {
                    imgMenu.setImageResource(R.drawable.home_work_icon_school)
                }

                Constant.M_EXAM -> {
                    imgMenu.setImageResource(R.drawable.exam_icon)
                }

                24 -> {
                    //imgMenu.setImageResource(R.drawable.assignment_icon_school)
                }

                Constant.M_NOTICEBOARD -> {
                    imgMenu.setImageResource(R.drawable.noticeboard_icon)
                }

                Constant.M_EVENTS_HOLIDAYS -> {
                    imgMenu.setImageResource(R.drawable.event_icon_school)
                }

                Constant.M_ATTENDANCE_REPORT -> {
                    imgMenu.setImageResource(R.drawable.attendance_report_icon)
                }

                Constant.M_LEAVE_REQUEST -> {
                    imgMenu.setImageResource(R.drawable.leave_request_icon_school)
                }

                Constant.M_FEE_DETAILS -> {
                    imgMenu.setImageResource(R.drawable.fee_details)
                }

                Constant.M_INTERACTION_WITH_STAFF -> {
                    //    imgMenu.setImageResource(R.drawable.interact_with_student)
                }

                15 -> {
                    // imgMenu.setImageResource(R.drawable.event_icon_school)
                }

                Constant.M_ASSIGNMENT -> {
                    imgMenu.setImageResource(R.drawable.assignment_icon_school)
                }

                19 -> {
//                    imgMenu.setImageResource(R.drawable.event_icon_school)
                }

                Constant.M_ONLINE_MEETING -> {
                    imgMenu.setImageResource(R.drawable.online_meeting_icon)
                }

                Constant.M_QUIZ_EXAM -> {
                    imgMenu.setImageResource(R.drawable.quiz_icon)
                }

                Constant.M_LSRW -> {
                    imgMenu.setImageResource(R.drawable.lsrw_icon)
                }

                Constant.M_CLASS_TIME_TABLE -> {
                    imgMenu.setImageResource(R.drawable.timetable_icon)
                }

                Constant.M_CERTIFICATE_REQUEST -> {
//                    imgMenu.setImageResource(R.drawable.)
                }
            }

            rlaMenu.setOnClickListener {
                Constant.SELECTED_PARENT_MENU = data.id
                listener.onClick(data)
            }
        }
    }

    class AdViewHolder(itemView: View, private val adapter: ChildMenuAdapter) :
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
//            lblSeeMore.setOnClickListener {
//                adapter.toggleMoreItems(lblSeeMore, rlaMenuExample)
//            }

            lnrAssignment.setOnClickListener {
                context.startActivity(Intent(context, Assignment::class.java))
            }
            lnrLeaveRequest.setOnClickListener {
                context.startActivity(
                    Intent(
                        context,
                        LeaveRequest::class.java
                    )
                )
            }
            lnrHomeWork.setOnClickListener {
                context.startActivity(Intent(context, HomeWork::class.java))
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

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}