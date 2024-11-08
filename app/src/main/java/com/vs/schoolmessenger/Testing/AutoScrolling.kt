package com.vs.schoolmessenger.Testing

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.Dashboard.School.PieChartAdapter
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.databinding.AutoScrollingBinding
import java.util.Timer
import java.util.TimerTask

class AutoScrolling : BaseActivity<AutoScrollingBinding>(), View.OnClickListener {

    override fun getViewBinding(): AutoScrollingBinding {
        return AutoScrollingBinding.inflate(layoutInflater)
    }

    var isFirstTime = true
    private lateinit var layoutManager: LinearLayoutManager
    private var urls: List<Int> = listOf()
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private var position: Int = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun setupViews() {
        super.setupViews()
        setupToolbar()

        getListUrls()
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvBanner.layoutManager = layoutManager
        val bannerAdapter = PieChartAdapter(this, urls)
        binding.rcvBanner.adapter = bannerAdapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcvBanner)

        // Set initial position to 0 for the first item
        if (urls.isNotEmpty()) {
            position = 0
            binding.rcvBanner.scrollToPosition(position)
        }

        // Run auto-scrolling
        runAutoScrollBanner()
        if (isFirstTime) {
            isFirstTime = false
            setupDots(urls.size)
        } else {
            setupDots(urls.size + 1)
        }

        binding.rcvBanner.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    override fun onResume() {
        super.onResume()
        runAutoScrollBanner()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScrollBanner()
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
                            binding.rcvBanner.smoothScrollToPosition(position % urls.size + 1) // Loop within the list size
                        } else {
                            binding.rcvBanner.smoothScrollToPosition(position % urls.size) // Loop within the list size
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
            val dot = ImageView(this).apply {
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

    override fun onClick(p0: View?) {
        // Implement onClick logic if needed
    }
}
