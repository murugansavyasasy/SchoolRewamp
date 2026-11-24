package com.vs.schoolmessenger.CommonScreens.Ads

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.vs.schoolmessenger.R

/**
 * Banner ad slider used on the School and Parent home screens.
 *
 * - shows only when the API status is true, ads are enabled (enable_ad && banner_ad)
 *   and at least one ad has an image
 * - rounded corners (item_ad_banner.xml)
 * - auto scrolls every 5 seconds, the user can also swipe manually
 *   (a manual swipe restarts the 5 second timer)
 * - tapping an ad opens its redirect_url (if any)
 */
class AdSlider(
    private val container: View,
    private val viewPager: ViewPager2,
    private val dotsLayout: LinearLayout,
    closeButton: View? = null
) {

    companion object {
        private const val AUTO_SCROLL_DELAY = 5000L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var ads: List<AdItem> = emptyList()
    private var isUserDragging = false
    private var isStarted = false

    // true after the user taps X: the ad stays hidden for this screen,
    // even if the ads API responds again
    private var isDismissed = false

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (ads.size > 1 && !isUserDragging) {
                viewPager.currentItem = (viewPager.currentItem + 1) % ads.size
            }
            handler.postDelayed(this, AUTO_SCROLL_DELAY)
        }
    }

    init {
        closeButton?.setOnClickListener {
            isDismissed = true
            hide()
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                restartTimer()          // manual or auto change -> wait 5s again
            }

            override fun onPageScrollStateChanged(state: Int) {
                isUserDragging = state == ViewPager2.SCROLL_STATE_DRAGGING
            }
        })
    }

    /** Bind the API response. status false / null / no ads -> slider is hidden. */
    fun bind(response: AdsResponse?) {
        // only the status decides: true -> show the ads, false / null -> hide.
        // ads_display_options is ignored (the options entry has no id, so it is filtered out)
        val list = if (response != null && response.status) {
            response.data.filter { it.id != null && !it.content_url.isNullOrBlank() }
        } else emptyList()

        if (list.isEmpty() || isDismissed) {
            hide()
            return
        }

        ads = list
        container.visibility = View.VISIBLE
        viewPager.adapter = AdAdapter(list)
        setupDots(list.size)
        updateDots(0)
        if (isStarted) restartTimer()
    }

    fun hide() {
        ads = emptyList()
        handler.removeCallbacks(autoScrollRunnable)
        container.visibility = View.GONE
    }

    /** Call from onResume. */
    fun start() {
        isStarted = true
        restartTimer()
    }

    /** Call from onPause / onDestroyView. */
    fun stop() {
        isStarted = false
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun restartTimer() {
        handler.removeCallbacks(autoScrollRunnable)
        if (isStarted && ads.size > 1) {
            handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY)
        }
    }

    private fun setupDots(count: Int) {
        dotsLayout.removeAllViews()
        if (count <= 1) {
            dotsLayout.visibility = View.GONE
            return
        }
        dotsLayout.visibility = View.VISIBLE
        val density = dotsLayout.resources.displayMetrics.density
        val size = (7 * density).toInt()
        val margin = (3 * density).toInt()
        repeat(count) {
            val dot = View(dotsLayout.context)
            dot.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(margin, 0, margin, 0)
            }
            dotsLayout.addView(dot)
        }
    }

    private fun updateDots(selected: Int) {
        for (i in 0 until dotsLayout.childCount) {
            dotsLayout.getChildAt(i).background = ContextCompat.getDrawable(
                dotsLayout.context,
                if (i == selected) R.drawable.bg_ad_dot_active else R.drawable.bg_ad_dot_inactive
            )
        }
    }

    private class AdAdapter(private val items: List<AdItem>) :
        RecyclerView.Adapter<AdAdapter.Holder>() {

        class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.imgAd)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_ad_banner, parent, false)
        )

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val ad = items[position]
            Glide.with(holder.image)
                .load(ad.content_url)
                .into(holder.image)

            holder.itemView.setOnClickListener {
                val url = ad.redirect_url?.trim()
                if (url.isNullOrEmpty()) return@setOnClickListener
                try {
                    it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
