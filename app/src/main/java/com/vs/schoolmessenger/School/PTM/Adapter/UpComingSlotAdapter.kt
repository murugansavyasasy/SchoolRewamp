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

        holder.lblDate.text = data.date
        holder.lblTitle.text = data.event_name
        holder.lblMode.text = "Mode - ${data.event_mode}"
        holder.lblTime.text = "${data.start_time} - ${data.end_time}"

        if (data.profiles.isNullOrEmpty()) {
            holder.img1.visibility = View.GONE
            holder.img2.visibility = View.GONE
            holder.img3.visibility = View.GONE
            holder.lblCount.visibility = View.GONE
            holder.tvNoProfiles.visibility = View.VISIBLE
        } else {
            holder.img1.visibility = View.VISIBLE
            holder.img2.visibility = View.VISIBLE
            holder.img3.visibility = View.VISIBLE
            holder.tvNoProfiles.visibility = View.GONE

            val defaultImg = R.drawable.user_sample
            val images = data.profiles.take(3)

            if (images.isNotEmpty()) holder.img1.setImageResource(defaultImg)
            if (images.size > 1) holder.img2.setImageResource(defaultImg)
            if (images.size > 2) holder.img3.setImageResource(defaultImg)

            if (data.profiles.size > 3) {
                holder.lblCount.visibility = View.VISIBLE
                holder.lblCount.text = "+${data.profiles.size - 3}"
            } else {
                holder.lblCount.visibility = View.GONE
            }
        }


        holder.rltJoinNow.setOnClickListener {
            val url = data.join_url ?: "https://www.google.com"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            listener.onClickListener(data)
        }
    }
}
