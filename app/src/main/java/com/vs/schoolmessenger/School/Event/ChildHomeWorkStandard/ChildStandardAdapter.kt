package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R


class ChildStandardAdapter(
    private var itemList: List<TargetData>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.child_standard_recyclerview, parent, false)
        return DataViewHolder(view, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val flattenedList = itemList.flatMap { it.name.map { n -> Pair(it.type, n) } }
            val item = flattenedList[position]
            holder.bind(item.first, item.second)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList.sumOf { it.name.size }
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        private val lblvalue: TextView = itemView.findViewById(R.id.lblvalue)

        fun bind(type: String, name: String) {
            lblvalue.text = "\uD83C\uDF93" +" "+ name
        }
    }
}

