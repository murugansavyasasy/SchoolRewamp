package com.vs.schoolmessenger.School.LSRW.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.School.LSRW.Model.TopPerformanceItem
import com.vs.schoolmessenger.databinding.ItemTopPerformanceBinding

class TopPerformanceAdapter(
    private val items: List<TopPerformanceItem>
) : RecyclerView.Adapter<TopPerformanceAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopPerformanceBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopPerformanceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            txtTitle.text = item.studentName
            txtclassname.text = item.className
            txtValue.text = "${item.percentage}%"

            val name = item.studentName
            avatarText.text = if (!name.isNullOrEmpty()) {
                val parts = name.trim().split("\\s+".toRegex())
                val initials = buildString {
                    append(parts.first().first().uppercaseChar())
                    if (parts.size > 1) append(parts.last().first().uppercaseChar())
                }
                initials
            } else {
                "-"
            }


        }
    }

    override fun getItemCount(): Int = items.size
}
