package com.vs.schoolmessenger.Dashboard.Fragments
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Dashboard.Model.GridItem
import com.vs.schoolmessenger.Dashboard.School.PieChartAdapter
import com.vs.schoolmessenger.Dashboard.School.SchoolMenuAdapter
import com.vs.schoolmessenger.Dashboard.Settings.Notification.Notification
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.databinding.SchoolHomeFragmentBinding
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: SchoolHomeFragmentBinding // Automatically generated binding class
    lateinit var isMenuAdapter: SchoolMenuAdapter
    private lateinit var items: List<GridItem>
    private var isMenuItems: MutableList<GridItem> = mutableListOf()
    private var isSearchVisible = false

    var isFirstTime = true
    private lateinit var layoutManager: LinearLayoutManager
    private var urls: List<Int> = listOf()
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var position: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isTouching = false


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = SchoolHomeFragmentBinding.inflate(layoutInflater)
        binding.imgNotification.setOnClickListener(this)
        binding.imgSearchClick.setOnClickListener(this)

        // Sample data
        items = listOf(
            GridItem(R.drawable.communication, "Communication"),  //Text,voice,Image ,Pdf , video
            GridItem(R.drawable.image, "Image/Pdf"),
            GridItem(R.drawable.video, "Video Upload"),
            GridItem(R.drawable.pdf, "Circulars"),
            GridItem(R.drawable.homework, "Homework"),
            GridItem(R.drawable.exam, "Schedule Exam/Test"),
            GridItem(R.drawable.chats, "Event"),
            GridItem(R.drawable.chats, "School Strength"),
            GridItem(R.drawable.noticeboard, "Notice Board"),
            GridItem(R.drawable.attendance, "Attendance Marking"),
            GridItem(R.drawable.messages, "Messages from management"),
            GridItem(R.drawable.leave, "Leave Requests"),
            GridItem(R.drawable.assignment, "Assignment"),
            GridItem(R.drawable.chats, "Interaction with student"),
            GridItem(R.drawable.online_meeting, "Online Meeting"),
            GridItem(R.drawable.student_image, "Student Report"),
            GridItem(R.drawable.splash_icon5, "Lesson Plan"),
            GridItem(R.drawable.meeting, "PTM"),
            GridItem(R.drawable.biometric_attendance, "Mark your attendance")
        )

        // Pie chart scrolling
        getListUrls()
        layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rcyAutoScrolling.layoutManager = layoutManager
        val bannerAdapter = PieChartAdapter(requireActivity(), urls)
        binding.rcyAutoScrolling.adapter = bannerAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcyAutoScrolling)

        // Set initial position to 0 for the first item
        if (urls.isNotEmpty()) {
            position = 0
            binding.rcyAutoScrolling.scrollToPosition(position)
        }

        // Run auto-scrolling
        runAutoScrollBanner()
        if (isFirstTime) {
            isFirstTime = false
            setupDots(urls.size)
        } else {
            setupDots(urls.size + 1)
        }

        binding.rcyAutoScrolling.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isFirstTime) {
                        isFirstTime = false
                        position =
                            layoutManager.findFirstCompletelyVisibleItemPosition() % urls.size + 1
                    } else {
                        position =
                            layoutManager.findFirstCompletelyVisibleItemPosition() % urls.size
                    }
                    updateDots(position)
                }
            }
        })
        // Delay the auto-scroll for better user experience (start scrolling after 2 seconds)
        handler.postDelayed({ runAutoScrollBanner() }, 4000)

        binding.rcyAutoScrolling.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start detecting long press
                    isTouching = true
                    stopAutoScrollBanner()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // User released touch or action canceled
                    isTouching = false
                    // Restart auto-scrolling if no long-press is happening
                    handler.postDelayed({ if (!isTouching) runAutoScrollBanner() }, 500)
                }
            }
            false
        }


        binding.txtSearchMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
        })

        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun filter(text: String) {
        isMenuItems.clear()
        if (text.isEmpty()) {
            isMenuItems.addAll(items)  // If search is empty, show all items
        } else {
            for (item in items) {
                if (item.title.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                    isMenuItems.add(item)  // Add the matching GridItem to filteredList
                }
            }
        }
        isMenuAdapter.notifyDataSetChanged()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imgNotification -> {
                val intent = Intent(requireActivity(), Notification::class.java)
                startActivity(intent)
            }

            R.id.imgSearchClick -> {

                if (isSearchVisible) {
                    isSearchVisible = false
                    binding.rytSearch.visibility = View.GONE
                } else {
                    isSearchVisible = true
                    binding.rytSearch.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        runAutoScrollBanner()

        val adapter = SchoolMenuAdapter(requireActivity(), null, Constant.isShimmerViewShow)
        binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.recyclerViewMenu.adapter = adapter
        Constant.executeAfterDelay(){
            val isAdapter =
                SchoolMenuAdapter(requireActivity(), items, Constant.isShimmerViewDisable)
            // Set GridLayoutManager (2 columns in this case)
            binding.recyclerViewMenu.layoutManager = GridLayoutManager(requireActivity(), 4)
            binding.recyclerViewMenu.adapter = isAdapter
        }

        Log.d("Status","onResume")
    }

    override fun onPause() {
        super.onPause()
        stopAutoScrollBanner()
        Constant.stopDelay()
        Log.d("Status","onPause")

    }

    private fun stopAutoScrollBanner() {
        timerTask?.cancel()
        timer?.cancel()
        timer = null
        timerTask = null
        position = layoutManager.findFirstCompletelyVisibleItemPosition()
    }


    private fun runAutoScrollBanner() {
        if (timer == null && timerTask == null) {
            timer = Timer()
            timerTask = object : TimerTask() {
                override fun run() {
                    handler.post {
                        position++
                        if (isFirstTime) {
                            isFirstTime = false
                            binding.rcyAutoScrolling.smoothScrollToPosition(position % urls.size + 1) // Loop within the list size
                        } else {
                            binding.rcyAutoScrolling.smoothScrollToPosition(position % urls.size) // Loop within the list size
                        }
                    }
                }
            }
            timer?.schedule(timerTask, 4000, 4000)
        }
    }

    private fun setupDots(count: Int) {
        binding.dotContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(requireActivity()).apply {
                setImageResource(if (i == position) R.drawable.active_dot else R.drawable.inactive_dot)
                val params = LinearLayout.LayoutParams(20, 20)
                params.setMargins(8, 0, 8, 0)
                layoutParams = params
            }
            binding.dotContainer.addView(dot)
        }
    }

    private fun updateDots(activePosition: Int) {
        for (i in 0 until binding.dotContainer.childCount) {
            val dot = binding.dotContainer.getChildAt(i) as ImageView
            dot.setImageResource(if (i == activePosition) R.drawable.active_dot else R.drawable.inactive_dot)
        }
    }

    private fun getListUrls() {
        // Here you can add your image resource IDs
        urls = listOf(
            R.drawable.splash_icon1,
            R.drawable.splash_icon2,
            R.drawable.splash_icon3,
            R.drawable.splash_icon4,
            R.drawable.splash_icon5,
            R.drawable.splash_icon6
        )
    }
}