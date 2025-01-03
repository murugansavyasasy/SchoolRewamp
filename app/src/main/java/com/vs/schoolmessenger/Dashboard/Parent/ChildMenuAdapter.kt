package com.vs.schoolmessenger.Dashboard.Parent

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
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
import com.vs.schoolmessenger.Dashboard.Model.AdItem
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Parent.Video.ParentVideo
import com.vs.schoolmessenger.R
import java.util.Timer
import java.util.TimerTask

class ChildMenuAdapter(
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
            position == 6 -> TYPE_AD
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DataViewHolder -> {
                holder.bind(itemList!!, position)
            }

            is AdViewHolder -> {
                if (position == 6) {
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
                    "Video" -> context.startActivity(Intent(context, ParentVideo::class.java))
                }
            }
        }
    }

    fun toggleMoreItems(lblSeeMore: TextView,rlaMenuExample:LinearLayout) {
        if (isSeeMore) {
            lblSeeMore.text = context.getString(R.string.SeeMore)
            rlaMenuExample.visibility=View.VISIBLE
            val startPosition = itemList!!.size - seeMoreMenus
            itemList!!.subList(startPosition, itemList!!.size).clear()
            notifyItemRangeRemoved(startPosition, seeMoreMenus)
            seeMoreMenus = 0
        } else {
            rlaMenuExample.visibility=View.GONE
            lblSeeMore.text =context.getString(R.string.SeeLess)
            val moreItems = getMoreItems()
            seeMoreMenus = moreItems.size
            val startPosition = itemList!!.size
            itemList!!.addAll(moreItems)
            notifyItemRangeInserted(startPosition, seeMoreMenus)
        }
        isSeeMore = !isSeeMore
    }

    class AdViewHolder(itemView: View, private val adapter: ChildMenuAdapter) :
        RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerViewAdImages)
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
            lblSeeMore.paintFlags = lblSeeMore.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            // Initialize layoutManager
            layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = AdImageAdapter(images)
            rlaMenuExample.visibility=View.VISIBLE
            lblSeeMore.setOnClickListener {
                adapter.toggleMoreItems(lblSeeMore,rlaMenuExample)
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

    private fun getMoreItems(): ArrayList<GridItem> {
        return arrayListOf(
            GridItem(R.drawable.chats, "Event / Holidays"),
            GridItem(R.drawable.chats, "Class Timetable"),
            GridItem(R.drawable.noticeboard, "Notice Board"),
            GridItem(R.drawable.attendance, "Attendance Report"),
            GridItem(R.drawable.messages, "Fee Details"),
            GridItem(R.drawable.leave, "Leave Requests"),
            GridItem(R.drawable.assignment, "Assignment"),
            GridItem(R.drawable.chats, "Interaction with student"),
            GridItem(R.drawable.online_meeting, "Online Meeting"),
            GridItem(R.drawable.meeting, "PTM"),
            GridItem(R.drawable.meeting, "LSRW"),
            GridItem(R.drawable.biometric_attendance, "Exam Marks")
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