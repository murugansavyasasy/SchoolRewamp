package com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListAdapter
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.GroupList.GroupListAdapter.ShimmerViewHolder
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.ShimmerUtil

class SectionListAdapter(
    private var itemList: List<Section>?,
    private var listener: SectionListClickListener,
    private var context: Context,
    private var isLoading: Boolean,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

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
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener)
        } else if (holder is SectionListAdapter.ShimmerViewHolder) {
            holder.startShimmer()
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else itemList!!.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        val lblgroupname: TextView = itemView.findViewById(R.id.lblgroupname)
        val chMultipleSchool: CheckBox = itemView.findViewById(R.id.chMultipleSchool)

        fun bind(
            data: Section,
            position: Int,
            listener: SectionListClickListener,
        ) {
            lblgroupname.text = data.name

            chMultipleSchool.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    listener.onIdCheck(data) // add to selected list
                } else {
                    listener.onIdUnchecked(data) // remove from selected list
                }
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}