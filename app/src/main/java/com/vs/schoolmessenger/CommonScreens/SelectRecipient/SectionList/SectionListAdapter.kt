package com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.SectionListClickListener
import com.vs.schoolmessenger.CommonScreens.SelectRecipient.SectionList.Section
import com.vs.schoolmessenger.R

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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.shimmer_view_small_list, parent, false)
            ShimmerViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.group_list_item, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 5 else itemList!!.size
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        val lblgroupname: TextView = itemView.findViewById(R.id.lblgroupname)
        val chName: CheckBox = itemView.findViewById(R.id.chMultipleSchool)
        fun bind(
            data: Section,
            position: Int,
            listener: SectionListClickListener,
        ) {
            lblgroupname.text = data.name

            chName.setOnCheckedChangeListener { _, isChecked ->
                listener.onSectionClick(data, isChecked)
            }
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmer_view_container)

        init {
            shimmerLayout.startShimmer()
        }
    }
}