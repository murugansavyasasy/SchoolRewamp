package com.vs.schoolmessenger.Parent.RequestLeave

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.util.Calendar

class CalendarAdapter(
    private val days: List<CalendarDay>,
    private val onDateClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val calendar = Calendar.getInstance().apply { time = day.date }
        holder.tvDay.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

        holder.tvDay.setTextColor(
            if (day.isCurrentMonth) Color.BLACK else Color.GRAY
        )

        holder.tvDay.setBackgroundResource(
            if (day.isSelected) R.drawable.bg_selected_day else android.R.color.transparent
        )

        holder.itemView.setOnClickListener {
            onDateClick(day)
        }
    }

    override fun getItemCount() = days.size
}
