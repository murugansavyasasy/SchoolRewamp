package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class DateAdapter(
    private val items: List<DateModel>,
    private val onDateClick: (DateModel) -> Unit
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date, parent, false)
        return DateViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date?.dayOfMonth?.toString() ?: ""

        val context = holder.itemView.context
        holder.tvDate.setBackgroundResource(
            if (item.date != null && item.date == items.find { it.isSelected }?.date) R.drawable.bg_selected_date
            else android.R.color.transparent
        )

        holder.tvDate.setOnClickListener {
            if (item.date != null) onDateClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
