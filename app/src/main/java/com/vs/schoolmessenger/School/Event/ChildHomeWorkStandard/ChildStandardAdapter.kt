package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.databinding.ChildStandardRecyclerviewBinding


class ChildStandardAdapter(
    private var itemList: List<SchoolNameTarget>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ChildStandardRecyclerviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            val school = itemList[position]
            holder.bind(school)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 3 else itemList.size
    }

    fun updateList(newList: List<SchoolNameTarget>) {
        this.itemList = newList
        this.isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(private val binding: ChildStandardRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SchoolNameTarget) {
            binding.lblvalue.text = item.getDisplayText()
        }
    }
}
