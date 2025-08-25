package com.vs.schoolmessenger.School.LSRW.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.LSRW.Model.WeeklyReportItem
import com.vs.schoolmessenger.databinding.ItemWeeklyReportBinding

class WeeklyReportAdapter(
    private val items: List<WeeklyReportItem>
) : RecyclerView.Adapter<WeeklyReportAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemWeeklyReportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWeeklyReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            txtWeekTitle.text = item.weekName
            txtWeekDesc.text = "${item.percentage}%"
        }
    }

    override fun getItemCount(): Int = items.size
}
