package com.vs.schoolmessenger.Dashboard.Settings

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.vs.schoolmessenger.Auth.Base.BaseActivity
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.PieChartView
import com.vs.schoolmessenger.databinding.ViewpagerTestingBinding

class VeiwPagerTesting : BaseActivity<ViewpagerTestingBinding>(), View.OnClickListener {


    private lateinit var viewPager: ViewPager2
    private lateinit var pieChart: PieChartView
    private lateinit var dotContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    // List of items for ViewPager
    private val items = listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")

  private  val chartData = listOf(
        Pair(20f, ContextCompat.getColor(this, R.color.yellow)),       // Girls 20%
        Pair(
            45f,
            ContextCompat.getColor(this, R.color.light_green_bg1)
        ), // Boys 50%
        Pair(
            35f,
            ContextCompat.getColor(this, R.color.light_orange0)
        )    // Staff 30%
    )

    private var currentIndex = 0

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            currentIndex = (currentIndex + 1) % items.size
            viewPager.setCurrentItem(currentIndex, true) // Automatically scroll ViewPager
            updatePieChartData(currentIndex)
            updateDots(currentIndex)
            handler.postDelayed(this, 2000) // Auto-scroll every 2 seconds
        }
    }


    override fun getViewBinding(): ViewpagerTestingBinding {
        return ViewpagerTestingBinding.inflate(layoutInflater)
    }

    override fun setupViews() {
        super.setupViews()
        isToolBarWhiteTheme()


        viewPager = findViewById(R.id.viewPager)
        pieChart = findViewById(R.id.pieChart)
        dotContainer = findViewById(R.id.dotContainer)

        // Setup ViewPager adapter
        val adapter = ViewPagerAdapter(this,items)
        viewPager.adapter = adapter

        // Setup dot indicators
        setupDots(items.size)

        // Update PieChart data for the 0th item
        updatePieChartData(0)

        // Start auto-scrolling
        handler.postDelayed(autoScrollRunnable, 2000)

    }


    private fun setupDots(count: Int) {
        dotContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setImageDrawable(getDotDrawable(i == currentIndex))
            dotContainer.addView(dot)
        }
    }

    // Update dot indicators based on the current active index
    private fun updateDots(activePosition: Int) {
        for (i in 0 until dotContainer.childCount) {
            val dot = dotContainer.getChildAt(i) as ImageView
            dot.setImageDrawable(getDotDrawable(i == activePosition))
        }
    }

    // Helper function to get dot drawable based on active/inactive state
    private fun getDotDrawable(isActive: Boolean) =
        ContextCompat.getDrawable(
            this,
            if (isActive) R.drawable.active_dot else R.drawable.inactive_dot
        )

    // Update PieChart data for the selected item (from ViewPager)
    private fun updatePieChartData(index: Int) {
        // Assuming that you will update pie chart data based on the current item in ViewPager
        // Here, for example, we always set the same chartData
        pieChart.setData(chartData)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoScrollRunnable) // Stop scrolling when activity is destroyed
    }


    override fun onClick(p0: View?) {

    }
}