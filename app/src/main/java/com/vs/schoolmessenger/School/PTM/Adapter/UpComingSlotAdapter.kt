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
import com.google.android.material.imageview.ShapeableImageView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.SlotDetail
import com.vs.schoolmessenger.School.PTM.InterFace.StaffSlotClickListener

class UpComingSlotAdapter(
    private val list: List<SlotDetail>?,
    private val context: Context,
    private val listener: StaffSlotClickListener,
    private val isShimmer: Boolean
) : RecyclerView.Adapter<UpComingSlotAdapter.SlotViewHolder>() {

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblDate: TextView = itemView.findViewById(R.id.lblDate)
        val lblTitle: TextView = itemView.findViewById(R.id.lblTitle)
        val lblMode: TextView = itemView.findViewById(R.id.lblMode)
        val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        val img1: ShapeableImageView = itemView.findViewById(R.id.img1)
        val img2: ShapeableImageView = itemView.findViewById(R.id.img2)
        val img3: ShapeableImageView = itemView.findViewById(R.id.img3)
        val lblCount: TextView = itemView.findViewById(R.id.lblCount)
        val rltJoinNow: RelativeLayout = itemView.findViewById(R.id.rltJoinNow)
        val rytSlots: RelativeLayout = itemView.findViewById(R.id.rytSlots)
        val tvNoProfiles: TextView = itemView.findViewById(R.id.tvNoProfiles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slots_item_staff_side, parent, false)
        return SlotViewHolder(view)
    }

    override fun getItemCount(): Int = list?.size ?: 0

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val data = list!![position]
        holder.lblDate.text = formatDate(data.date)

        holder.lblTitle.text = data.event_name
        holder.lblMode.text = "Mode - ${data.event_mode}"
        holder.lblTime.text = "${data.start_time} - ${data.end_time}"

        val profiles = data.profiles.map { it.toString() }
        if (profiles.isEmpty()) {
            holder.img1.visibility = View.GONE
            holder.img2.visibility = View.GONE
            holder.img3.visibility = View.GONE
            holder.lblCount.visibility = View.GONE
            holder.tvNoProfiles.visibility = View.VISIBLE
        } else {
            holder.tvNoProfiles.visibility = View.GONE
            holder.img1.visibility = View.GONE
            holder.img2.visibility = View.GONE
            holder.img3.visibility = View.GONE
            holder.lblCount.visibility = View.GONE

            for (i in profiles.indices.take(3)) {
                val imageUrl = profiles[i]
                val imageView = when(i) {
                    0 -> holder.img1
                    1 -> holder.img2
                    2 -> holder.img3
                    else -> holder.img1
                }

                if (imageUrl.isNotBlank() && imageUrl != "null") {
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
                holder.lblCount.visibility = View.VISIBLE
                holder.lblCount.text = "+${profiles.size - 3}"
            }
        }

        holder.rltJoinNow.setOnClickListener {
            val url = data.join_url ?: "https://www.google.com"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            listener.onClickListener(data)
        }
    }

    private fun formatDate(apiDate: String?): String {
        if (apiDate.isNullOrBlank()) return ""

        val raw = apiDate.trim()
        android.util.Log.d("UpComingSlotAdapter", "formatDate input: $raw")

        if (raw.matches(Regex("^\\d{10}$")) || raw.matches(Regex("^\\d{13}$"))) {
            try {
                val millis = if (raw.length == 10) raw.toLong() * 1000L else raw.toLong()
                val d = java.util.Date(millis)
                val out = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(d)
                android.util.Log.d("UpComingSlotAdapter", "parsed epoch -> $out")
                return out
            } catch (e: Exception) {
            }
        }

        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "MM/dd/yyyy", "dd MMM yyyy", "dd MMM yy", "dd-MM-yy", "yyyy/MM/dd")

        for (pattern in patterns) {
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(raw)
                if (parsed != null) {
                    val out = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(parsed)
                    android.util.Log.d("UpComingSlotAdapter", "parsed with $pattern -> $out")
                    return out
                }
            } catch (e: Exception) { /* ignore and try next */ }
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
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(fixed)
                if (parsed != null) {
                    val out = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(parsed)
                    android.util.Log.d("UpComingSlotAdapter", "fixed 2-digit year -> $out")
                    return out
                }
            } catch (e: Exception) { /* ignore */ }
        }
        android.util.Log.w("UpComingSlotAdapter", "Unable to parse date: $raw")
        return raw
    }


}
