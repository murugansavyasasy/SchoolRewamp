package com.vs.schoolmessenger.School.Hostel.Adapter.HotelList


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.HostelDashboard
import com.vs.schoolmessenger.School.Hostel.Model.HostelList.HostelListData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class HostelListAdapter(
    private var itemList: List<HostelListData>?,
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

    fun updateData(newList: List<HostelListData>) {
        itemList = newList
        notifyDataSetChanged()
    }


    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblHostelName: TextView = itemView.findViewById(R.id.lblHostelName)
        private val lblSchoolName: TextView = itemView.findViewById(R.id.lblSchoolName)
        private val lblPlace: TextView = itemView.findViewById(R.id.lblPlace)
        private val lnrHeader: LinearLayout = itemView.findViewById(R.id.lnrHeader)



        fun bind(data: HostelListData, position: Int) {
            lblHostelName.text = data.HostelName
            lblSchoolName.text = data.SchoolName
            lblPlace.text = data.Place
            lnrHeader.setOnClickListener {
                val intent = Intent(context, HostelDashboard::class.java)
                context.startActivity(intent)
            }
        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}