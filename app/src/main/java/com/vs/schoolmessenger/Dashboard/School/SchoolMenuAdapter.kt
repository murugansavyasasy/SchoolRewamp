package com.vs.schoolmessenger.Dashboard.School

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.CommonScreens.Ads.AdItem
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuClickListener
import com.vs.schoolmessenger.CommonScreens.MenuDetails.MenuDetail
import com.vs.schoolmessenger.Dashboard.Parent.AdImageAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil
import java.util.Timer
import java.util.TimerTask


class SchoolMenuAdapter(
    private var context: Context,
    private var listener: MenuClickListener,
    private var itemList: List<MenuDetail>?,
    private var isAdItem: List<AdItem>?,
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
                val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.menu_item_card)
                ShimmerViewHolder(shimmerView)
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
                itemList?.get(position)?.let { menuDetail ->
                    holder.bind(menuDetail, listener)
                }
            }

            is ShimmerViewHolder -> {
                holder.startShimmer()
            }

            is AdViewHolder -> {
                if (position == 9) {
                    Log.d("isAddItem", isAdItem!!.size.toString())
                    holder.bind(isAdItem!!, context)
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

        fun bind(data: MenuDetail, listener: MenuClickListener) {
            lblMenuName.text = data.name

            when (data.id) {

                0 -> {
                    imgMenu.setImageResource(R.drawable.communication_icon_dashboard)
                }

                22 -> {
                    imgMenu.setImageResource(R.drawable.assignment_icon_school)
                }

                9 -> {
                    imgMenu.setImageResource(R.drawable.home_work_icon_school)
                }

                12 -> {
                    imgMenu.setImageResource(R.drawable.attendance_marking)
                }

                6 -> {
                    imgMenu.setImageResource(R.drawable.absentees_report)
                }

                7 -> {
                    imgMenu.setImageResource(R.drawable.school_strength)
                }

                3 -> {
                    imgMenu.setImageResource(R.drawable.noticeboard_icon)
                }

                4 -> {
                    imgMenu.setImageResource(R.drawable.event_icon_school)
                }

                11 -> {
                    imgMenu.setImageResource(R.drawable.exam_icon)
                }

                13 -> {
                    imgMenu.setImageResource(R.drawable.message_f_management)
                }

                16 -> {
                    imgMenu.setImageResource(R.drawable.interact_with_student)
                }

                26 -> {
                    imgMenu.setImageResource(R.drawable.online_meeting_icon)
                }

                28 -> {
                    imgMenu.setImageResource(R.drawable.daily_collection)
                }

                29 -> {
                    imgMenu.setImageResource(R.drawable.student_report)
                }

                30 -> {
                    imgMenu.setImageResource(R.drawable.lesson_plan)
                }

                14 -> {
                    imgMenu.setImageResource(R.drawable.fee_pending_reports)
                }

                21 -> {
                    imgMenu.setImageResource(R.drawable.importent_info)
                }
            }

            rlaMenu.setOnClickListener {
                listener.onClick(data)

            }
        }
    }

//    fun toggleMoreItems(lblSeeMore: TextView, rlaMenuExample: LinearLayout) {
//        if (isSeeMore) {
//            lblSeeMore.text = context.getString(R.string.SeeAll)
//            rlaMenuExample.visibility = View.GONE
//            val startPosition = itemList!!.size - seeMoreMenus
////            itemList!!.subList(startPosition, itemList!!.size).clear()
//            notifyItemRangeRemoved(startPosition, seeMoreMenus)
//            seeMoreMenus = 0
//        } else {
//            rlaMenuExample.visibility = View.GONE
//            lblSeeMore.text = context.getString(R.string.SeeLess)
//            val moreItems = getMoreItems()
//            seeMoreMenus = moreItems.size
//            val startPosition = itemList!!.size
////            itemList!!.addAll(moreItems)
//            notifyItemRangeInserted(startPosition, seeMoreMenus)
//        }
//        isSeeMore = !isSeeMore
//    }

//    fun toggleMoreItems() {
//        seeMoreMenus = 0
//        val moreItems = getMenu()
//        seeMoreMenus = moreItems.size
////        itemList!!.addAll(moreItems)
//    }

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

        fun bind(isAds: List<AdItem>, context: Context) {
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = AdImageAdapter(isAds)
            rlaMenuExample.visibility = View.GONE

//            lblSeeMore.setOnClickListener {
//                adapter.toggleMoreItems(lblSeeMore, rlaMenuExample)
//            }

//            lnrAssignment.setOnClickListener {
//                context.startActivity(Intent(context, Assignment::class.java))
//            }
//            lnrLeaveRequest.setOnClickListener {
//                context.startActivity(
//                    Intent(
//                        context, LeaveRequests::class.java
//                    )
//                )
//            }
//            lnrHomeWork.setOnClickListener {
//                context.startActivity(
//                    Intent(
//                        context, com.vs.schoolmessenger.Parent.Homework.HomeWork::class.java
//                    )
//                )
//            }

//            runAutoScrollBanner(images)
//            if (isFirstTime) {
//                isFirstTime = false
//                setupDots(images.size, context)
//            } else {
//                setupDots(images.size + 1, context)
//            }

//            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                    super.onScrollStateChanged(recyclerView, newState)
//                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                        if (isFirstTime) {
//                            isFirstTime = false
//                            position =
//                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size + 1
//                        } else {
//                            position =
//                                layoutManager.findFirstCompletelyVisibleItemPosition() % images.size
//                        }
//                        updateDots(position)
//                    }
//                }
//            })
//
//            // Delay the auto-scroll for better user experience (start scrolling after 2 seconds)
//            handler.postDelayed({ runAutoScrollBanner(images) }, 3000)
//
//            recyclerView.setOnTouchListener { _, event ->
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        isTouching = true
//                        stopAutoScrollBanner()
//                    }
//
//                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                        isTouching = false
//                        handler.postDelayed({ if (!isTouching) runAutoScrollBanner(images) }, 500)
//                    }
//                }
//                false
//            }
//
//            adapter.toggleMoreItems()
        }

//        private fun setupDots(count: Int, context: Context) {
//            dotContainer.removeAllViews()
//            for (i in 0 until count) {
//                val dot = ImageView(context).apply {
//                    setImageResource(if (i == position) R.drawable.active_dot else R.drawable.inactive_dot)
//                    val params = LinearLayout.LayoutParams(20, 20)
//                    params.setMargins(8, 0, 8, 0)
//                    layoutParams = params
//                }
//                dotContainer.addView(dot)
//            }
//        }

//        private fun updateDots(activePosition: Int) {
//            for (i in 0 until dotContainer.childCount) {
//                val dot = dotContainer.getChildAt(i) as ImageView
//                dot.setImageResource(if (i == activePosition) R.drawable.active_dot else R.drawable.inactive_dot)
//            }
//        }

//        private fun stopAutoScrollBanner() {
//            timerTask?.cancel()
//            timer?.cancel()
//            timer = null
//            timerTask = null
//            position = layoutManager.findFirstCompletelyVisibleItemPosition()
//        }

//        private fun runAutoScrollBanner(images: List<AdItem>) {
//            if (timer == null && timerTask == null) {
//                timer = Timer()
//                timerTask = object : TimerTask() {
//                    override fun run() {
//                        handler.post {
//                            position++
//                            if (isFirstTime) {
//                                isFirstTime = false
//                                recyclerView.smoothScrollToPosition(position % images.size + 1) // Loop within the list size
//                            } else {
//                                recyclerView.smoothScrollToPosition(position % images.size) // Loop within the list size
//                            }
//                        }
//                    }
//                }
//                timer?.schedule(timerTask, 3000, 3000)
//            }
//        }
//        private fun runAutoScrollBanner(images: List<AdItem>) {
//            if (timer == null && timerTask == null) {
//                timer = Timer()
//                timerTask = object : TimerTask() {
//                    override fun run() {
//                        handler.post {
//                            position++
//                            if (isFirstTime) {
//                                isFirstTime = false
//                                recyclerView.smoothScrollToPosition(position % images.size + 1) // Loop within the list size
//                            } else {
//                                recyclerView.smoothScrollToPosition(position % images.size) // Loop within the list size
//                            }
//                        }
//                    }
//                }
//                timer?.schedule(timerTask, 3000, 3000)
//            }
//        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}