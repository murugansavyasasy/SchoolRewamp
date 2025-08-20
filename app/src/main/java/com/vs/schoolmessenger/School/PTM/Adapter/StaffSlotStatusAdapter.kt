package com.vs.schoolmessenger.School.PTM.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.Slot
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StaffSlotStatusAdapter(
    private var itemList: List<Slot>? = null,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.staff_slot_status_item)
            ShimmerViewHolder(
                shimmerView
            )
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.staff_slot_status_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            // Bind actual data when loading is complete
            holder.bind(itemList!![position], position)

        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 // Show shimmer items while loading
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblStatus: TextView = itemView.findViewById(R.id.lblStatus)
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblDuration: TextView = itemView.findViewById(R.id.lblDuration)
        private val lblBookedName: TextView = itemView.findViewById(R.id.lblBookedName)
        private val lblStandardAndSection: TextView =
            itemView.findViewById(R.id.lblStandardAndSection)

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: Slot, position: Int) {
            lblBookedName.text = data.booked_by
            lblStatus.text = data.status
            lblDuration.text = "Meeting Duration" + " - " + data.meeting_duration.toString()
            lblTime.text = data.from_time + " - " + data.to_time
            lblStandardAndSection.text = data.my_class + " - " + data.my_section

        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}