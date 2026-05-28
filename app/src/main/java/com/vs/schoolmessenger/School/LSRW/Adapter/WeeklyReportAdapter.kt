package com.vs.schoolmessenger.School.LSRW.Adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.LSRW.Model.WeeklyReportItem
import com.vs.schoolmessenger.databinding.ItemWeeklyReportBinding

class WeeklyReportAdapter(
    private val items: List<WeeklyReportItem>
) : RecyclerView.Adapter<WeeklyReportAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemWeeklyReportBinding) :
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
            txtweek.text = item.weekName
            txtpercentage.text = "${item.percentage}%"
            progressBar.max = 100
            progressBar.progress = item.percentage

            progressBar.progressTintList =
                ColorStateList.valueOf(ContextCompat.getColor(root.context, R.color.PrimaryColor))
        }
    }


    override fun getItemCount(): Int = items.size
}