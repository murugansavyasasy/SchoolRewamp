package com.vs.schoolmessenger.School.LSRW.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class LsrwFilterAdapter(
    private var filters: List<String>,
    private val onFilterClick: (String) -> Unit
) : RecyclerView.Adapter<LsrwFilterAdapter.FilterViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_chip, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = position
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onFilterClick(filter)
        }
    }

    override fun getItemCount(): Int = filters.size

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtFilter: TextView = itemView.findViewById(R.id.txtFilter)

        fun bind(filter: String, isSelected: Boolean) {
            txtFilter.text = filter
            if (isSelected) {
                txtFilter.setTextColor(Color.WHITE)
                txtFilter.setBackgroundResource(R.drawable.bg_filter_selected)
            } else {
                txtFilter.setTextColor(Color.BLACK)
                txtFilter.setBackgroundResource(R.drawable.bg_filter_unselected)
            }
        }
    }
}