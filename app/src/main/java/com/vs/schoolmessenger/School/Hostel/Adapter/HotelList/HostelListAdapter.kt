package com.vs.schoolmessenger.School.Hostel.Adapter.HotelList


import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Listner.HostelClickListner
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.getHostelListData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class HostelListAdapter(
    private var itemList: List<getHostelListData>?,
    private var context: Context,
    private val listener: HostelClickListner,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.hostel_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hostel_list_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<getHostelListData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblHostelName: TextView = itemView.findViewById(R.id.lblHostelName)
        private val lblSchoolName: TextView = itemView.findViewById(R.id.lblSchoolName)
        private val lblHostelType: TextView = itemView.findViewById(R.id.lblHostelType)
        private val lblRoomType: ImageView = itemView.findViewById(R.id.lblRoomType)
        private val lblAddress: TextView = itemView.findViewById(R.id.lblAddress)
        private val lblOccupacy: TextView = itemView.findViewById(R.id.lblOccupacy)
        private val cardHeader: MaterialCardView = itemView.findViewById(R.id.cardHeader)
        private val lblTotalCataory: TextView = itemView.findViewById(R.id.lblTotalCataory)
        private val lblHostelIDNO: TextView = itemView.findViewById(R.id.lblHostelIDNO)
        private val proOccupancyProgressBar: ProgressBar = itemView.findViewById(R.id.proOccupancyProgressBar)



        fun bind(data: getHostelListData, position: Int) {
            lblHostelName.text = data.name
            lblSchoolName.text = data.institute_name
            lblAddress.text = data.address
            lblTotalCataory.text = data.max_capacity
            lblHostelIDNO.text = "#${data.id}"
            lblHostelType.text = data.type

            when (data.type.lowercase()) {

                "female","girls" -> {

                    lblRoomType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_pink_1))

                    lblHostelType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_pink_1))

                    lblRoomType.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_dark_pink_1))

                    lblHostelType.setTextColor(
                        ContextCompat.getColor(context, R.color.light_dark_pink_1)
                    )
                }

                "male","boys" -> {

                    lblRoomType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_very_blue_1))

                    lblHostelType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_very_blue_1))

                    lblRoomType.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_dark_blue_1))

                    lblHostelType.setTextColor(
                        ContextCompat.getColor(context, R.color.light_dark_blue_1)
                    )
                }

                else -> {
                    lblRoomType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_very_blue_1))

                    lblHostelType.backgroundTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_very_blue_1))

                    lblRoomType.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_dark_blue_1))

                    lblHostelType.setTextColor(
                        ContextCompat.getColor(context, R.color.light_dark_blue_1)
                    )
                }
            }


            lblOccupacy.visibility= View.GONE
            proOccupancyProgressBar.visibility= View.GONE
            cardHeader.setOnClickListener {
                listener.onHostelClick(data)
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}