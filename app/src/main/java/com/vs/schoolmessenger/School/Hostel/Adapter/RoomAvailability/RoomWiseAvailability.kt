package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAvailability

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.FragmentType
import com.vs.schoolmessenger.School.Hostel.Model.HostelDashboard.RoomAvailabaility.getRoomAvailability
import com.vs.schoolmessenger.Utils.ShimmerUtil
import kotlin.math.log
import kotlin.math.roundToInt

class RoomWiseAvailability(
    private var itemList: List<getRoomAvailability>?,
    private var context: Context,
    private val hostelClickListner: HostelClickListner,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<getRoomAvailability> = itemList ?: listOf()
    private var filteredList: List<getRoomAvailability> = itemList ?: listOf()

    init {
        fullList = itemList ?: listOf()
        filteredList = fullList
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.roomwise_availability)
            ShimmerViewHolder(shimmerView)
        } else {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.roomwise_availability, parent, false)
            DataViewHolder(view, context, hostelClickListner)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position], position)

        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else filteredList?.size ?: 0
    }

    fun updateData(newList: List<getRoomAvailability>) {
        this.fullList = newList
        notifyDataSetChanged()
    }

    class DataViewHolder(
        itemView: View,
        private val context: Context,
        private val hostelClickListner: HostelClickListner
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val lblRoomNo: TextView = itemView.findViewById(R.id.lblRoomNo)
        private val lblBedCount: TextView = itemView.findViewById(R.id.lblBedCount)
        private val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)
        private val lblPersonStrength: TextView = itemView.findViewById(R.id.lblPersonStrength)


        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(data: getRoomAvailability, position: Int) {

            lblRoomNo.text = data.number
            lblBedCount.text = data.total_beds.toString()

            lblPersonStrength.text = "${data.current_occupancy} / ${data.max_occupancy}"

            cardHeader.setOnClickListener {
                hostelClickListner.onRoomClick(data)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}