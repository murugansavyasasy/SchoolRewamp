package com.vs.schoolmessenger.Parent.Hostel.Adapter.HostelInformation



import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.vs.schoolmessenger.Parent.Hostel.Model.ParentHostelDashboard.HostelInfo

class HostelInfoAdapter(
    private var itemList: List<HostelInfo>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<HostelInfo> = itemList ?: listOf()
    private var filteredList: List<HostelInfo> = fullList

    override fun getItemViewType(position: Int) = if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.parent_hostel_info)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.parent_hostel_info, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount() = if (isLoading) 5 else filteredList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position])
        }
    }

    fun updateData(newList: List<HostelInfo>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {

        fun bind(info: HostelInfo) {

            setRow(R.id.hostel_id, "Hostel ID", info.hostel_id)
            setRow(R.id.hostel_name, "Hostel Name", info.hostel_name)
            setRow(R.id.hostel_type, "Hostel Type", info.hostel_type)
            setRow(R.id.no_of_floors, "No of Floors", info.no_of_floors.toString())
            setRow(R.id.no_of_rooms, "No of Rooms", info.no_of_rooms.toString())
            setRow(R.id.warden_type, "Warden Type", info.warden_type)
            setRow(R.id.max_capacity, "Max Capacity", info.max_capacity.toString())
            setRow(R.id.warden_name, "Warden Name", info.warden_name.joinToString(", "))
            setRow(R.id.institute_name, "Institute Name", info.institute_name)
            setRow(R.id.institute_address, "Institute Address", info.institute_address)

        }

        private fun setRow(includeId: Int, key: String, value: String) {

            val container = itemView.findViewById<View>(includeId) ?: return

            val lblKey = container.findViewById<TextView>(R.id.lblKey)
            val lblValue = container.findViewById<TextView>(R.id.lblValue)

            lblKey.text = key
            lblValue.text = value
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}