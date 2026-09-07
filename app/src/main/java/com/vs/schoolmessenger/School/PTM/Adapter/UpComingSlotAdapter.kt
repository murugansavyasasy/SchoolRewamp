package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener

class UpComingSlotAdapter(
    private var list: List<SlotDetail>?,
    private val context: Context,
    private val listener: StaffSlotClickListener,
    private val isShimmer: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isShimmer) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_big_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.slots_item_staff_side, parent, false)
            SlotViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isShimmer) 5 else list?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        } else if (holder is SlotViewHolder) {
            val data = list?.get(position) ?: return
            holder.bind(data)
        }
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        private val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        private val lblMode: TextView = itemView.findViewById(R.id.lblMode)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val img1: ShapeableImageView = itemView.findViewById(R.id.img1)
        private val img2: ShapeableImageView = itemView.findViewById(R.id.img2)
        private val img3: ShapeableImageView = itemView.findViewById(R.id.img3)
        private val lblCount: TextView = itemView.findViewById(R.id.lblCount)
        private val rltJoinNow: RelativeLayout = itemView.findViewById(R.id.rltJoinNow)
        private val tvNoProfiles: TextView = itemView.findViewById(R.id.tvNoProfiles)
        private val imgDot: View = itemView.findViewById(R.id.imgDot)

        fun bind(data: SlotDetail) {
            lblDate.text = formatDate(data.date)
            lblTitle.text = data.event_name
            lblMode.text = "Mode - ${data.event_mode}"
            lblTime.text = "${data.start_time} - ${data.end_time}"

            val profiles = data.profiles.map { it }
            img1.visibility = View.GONE
            img2.visibility = View.GONE
            img3.visibility = View.GONE
            lblCount.visibility = View.GONE
            if (data.profiles.isEmpty()) {
                tvNoProfiles.visibility = View.VISIBLE
            } else {
                tvNoProfiles.visibility = View.GONE

                for (i in profiles.indices.take(3)) {
                    val imageView = when (i) {
                        0 -> img1
                        1 -> img2
                        2 -> img3
                        else -> img1
                    }
                    val imageUrl = profiles[i]
                    if (!imageUrl.isNullOrBlank() && imageUrl != "null") {
                        Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.user_sample)
                            .error(R.drawable.user_sample)
                            .into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.user_sample)
                    }
                    imageView.visibility = View.VISIBLE
                }

                if (profiles.size > 3) {
                    lblCount.visibility = View.VISIBLE
                    lblCount.text = "+${profiles.size - 3}"
                }
            }

            rltJoinNow.setOnClickListener {
                val url = data.join_url ?: "https://www.google.com"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }

            imgDot.setOnClickListener { listener.onSlotCancelReOpenClick(data, it) }

            itemView.setOnClickListener { listener.onClickListener(data) }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        fun startShimmer() {
            shimmerLayout.startShimmer()
        }
    }

    private fun formatDate(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return ""

        val raw = apiDate.trim()
        if (raw.matches(Regex("^\\d{10}$")) || raw.matches(Regex("^\\d{13}$"))) {
            try {
                val millis = if (raw.length == 10) raw.toLong() * 1000L else raw.toLong()
                val d = java.util.Date(millis)
                val out = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                    .format(d)
                return out
            } catch (e: Exception) {
            }
        }

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "MM/dd/yyyy",
            "dd MMM yyyy",
            "dd MMM yy",
            "dd-MM-yy",
            "yyyy/MM/dd"
        )

        for (pattern in patterns) {
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(raw)
                if (parsed != null) {
                    val out =
                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                            .format(parsed)
                    return out
                }
            } catch (e: Exception) { /* ignore and try next */
            }
        }

        val twoDigitYearMatch = Regex("([\\d]{1,2}[\\-/][\\d]{1,2}[\\-/])(\\d{2})\$").find(raw)
        if (twoDigitYearMatch != null) {
            val prefix = twoDigitYearMatch.groupValues[1]
            val yy = twoDigitYearMatch.groupValues[2].toInt()
            val fullYear = if (yy < 50) 2000 + yy else 1900 + yy
            val fixed = prefix + fullYear.toString()
            val sep = if (prefix.contains("/")) "/" else "-"
            val pattern = "dd${sep}MM${sep}yyyy"
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(fixed)
                if (parsed != null) {
                    val out =
                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
                            .format(parsed)
                    return out
                }
            } catch (e: Exception) { /* ignore */
            }
        }
        return raw
    }
}
