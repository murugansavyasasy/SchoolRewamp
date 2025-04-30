package com.vs.schoolmessenger.School.MarkYourAttendance.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.TextHistoryAdapter
import com.vs.schoolmessenger.School.MarkYourAttendance.DataClass.PunchTimingsData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class PunchHistoryAdapter(
    private var itemList: List<PunchTimingsData>?,
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
                ShimmerUtil.wrapWithShimmer(parent, R.layout.punch_history_item)
            TextHistoryAdapter.DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.punch_history_item, parent, false)
            DataViewHolder(view, context) // Pass context to DataViewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, this) // Pass adapter reference
        } else if (holder is TextHistoryAdapter.DataViewHolder.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20
        else itemList?.size ?: 0
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblTime: TextView = itemView.findViewById(R.id.lblTime)
        private val lblMobile: TextView = itemView.findViewById(R.id.lblMobile)
        private val lblType: TextView = itemView.findViewById(R.id.lblType)

        fun bind(
            data: PunchTimingsData,
            position: Int,
            adapter: PunchHistoryAdapter
        ) {
            lblTime.text = data.time
            lblMobile.text = data.device_model
            lblType.text = data.punch_type.value

        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}