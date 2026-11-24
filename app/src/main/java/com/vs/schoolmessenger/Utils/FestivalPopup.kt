package com.vs.schoolmessenger.Utils

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.vs.schoolmessenger.CommonScreens.MenuDetails.FestivalDetails
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Festival popup for the school and parent dashboards.
 * Shown when the menus API returns "is_festival": true, using "festival_details".
 *
 *  - url given   -> festival image fills the top area
 *  - url empty   -> default blue sparkle on a light blue background
 *  - name        -> blue title, date -> "24 Nov 2026", content -> shown only when sent
 *  - closes with the X button or a tap outside the card
 */
object FestivalPopup {

    fun show(activity: Activity, details: FestivalDetails?) {
        if (activity.isFinishing || activity.isDestroyed) return

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val view = LayoutInflater.from(activity).inflate(R.layout.festival_popup, rootView, false)
        rootView.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val txtName: TextView = view.findViewById(R.id.txtFestivalName)
        val txtContent: TextView = view.findViewById(R.id.txtFestivalContent)
        val txtDate: TextView = view.findViewById(R.id.txtFestivalDate)
        val imgFestival: ImageView = view.findViewById(R.id.imgFestival)
        val imgDefault: ImageView = view.findViewById(R.id.imgFestivalDefault)

        // name
        val name = details?.name?.trim().orEmpty()
        txtName.text = name
        txtName.visibility = if (name.isEmpty()) View.GONE else View.VISIBLE

        // content (optional)
        val content = details?.content?.trim().orEmpty()
        txtContent.text = content
        txtContent.visibility = if (content.isEmpty()) View.GONE else View.VISIBLE

        // date "2026-11-24" -> "24 Nov 2026"
        val date = formatDate(details?.date)
        txtDate.text = date
        txtDate.visibility = if (date.isEmpty()) View.GONE else View.VISIBLE

        // image: url -> festival image, otherwise (or if it fails) the default sparkle
        showDefaultImage(imgFestival, imgDefault)
        val url = details?.url?.trim().orEmpty()
        if (url.isNotEmpty()) {
            Glide.with(activity)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?,
                        target: Target<Drawable>, isFirstResource: Boolean
                    ): Boolean {
                        showDefaultImage(imgFestival, imgDefault)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable, model: Any, target: Target<Drawable>?,
                        dataSource: DataSource, isFirstResource: Boolean
                    ): Boolean {
                        imgFestival.visibility = View.VISIBLE
                        imgDefault.visibility = View.GONE
                        return false
                    }
                })
                .into(imgFestival)
        }

        // close: X button or tap outside the card (taps on the card itself are ignored)
        val close = { rootView.removeView(view) }
        view.findViewById<View>(R.id.btnFestivalClose).setOnClickListener { close() }
        view.findViewById<View>(R.id.festivalRoot).setOnClickListener { close() }
        view.findViewById<View>(R.id.cardFestival).setOnClickListener { /* keep open */ }

        view.alpha = 0f
        view.animate().alpha(1f).setDuration(250).start()
    }

    private fun showDefaultImage(imgFestival: ImageView, imgDefault: ImageView) {
        imgFestival.visibility = View.GONE
        imgDefault.visibility = View.VISIBLE
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(raw.trim())
            if (parsed != null) SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(parsed)
            else raw
        } catch (e: Exception) {
            raw
        }
    }
}
