package com.vs.schoolmessenger.CommonScreens.SelectRecipient.StandardList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class StandardListAdapter(
    internal var itemList: List<Standard>?,
    private var listener: StandardListClickListener,
    private var context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    private val selectedIds = mutableSetOf<Int>()

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.group_list_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.group_list_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position)
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else itemList?.size ?: 0
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblGroupName: TextView = itemView.findViewById(R.id.lblgroupname)
        private val chMultipleSchool: CheckBox = itemView.findViewById(R.id.chMultipleSchool)

        fun bind(data: Standard, position: Int) {
            lblGroupName.text = data.name

            chMultipleSchool.setOnCheckedChangeListener(null)
            chMultipleSchool.isChecked = selectedIds.contains(data.id)

            chMultipleSchool.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedIds.add(data.id)
                    listener.onIdCheck(data)
                } else {
                    selectedIds.remove(data.id)
                    listener.onIdUnchecked(data)
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }

    // Helper functions
    fun selectAll() {
        itemList?.forEach {
            selectedIds.add(it.id)
        }
        notifyDataSetChanged()
    }

    fun deselectAll() {
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<Int> {
        return selectedIds.toList()
    }
}
