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

class HostelInfoAdapter(
    private var itemList: List<String>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<String> = itemList ?: listOf()
    private var filteredList: List<String> = fullList

    override fun getItemViewType(position: Int) = if (isLoading) TYPE_SHIMMER else TYPE_DATA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_hostel_info)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hostel_info, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount() = if (isLoading) 5 else filteredList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position])
        }
    }

    fun updateData(newList: List<String>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
        private val lblKey: TextView = itemView.findViewById(R.id.lblKey)
        private val lblValue: TextView = itemView.findViewById(R.id.lblValue)

        fun bind(info: String) {
            val splitIndex = info.indexOf(":")
            if (splitIndex != -1) {
                val key = info.substring(0, splitIndex).trim()
                val value = info.substring(splitIndex + 1).trim()
                lblKey.text = key
                lblValue.text = value
            } else {
                lblKey.text = info
                lblValue.text = ""
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}