package com.vs.schoolmessenger.Parent.Homework.HomeWorkAdapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass.CalendarDate
import com.vs.schoolmessenger.R
class CalendarAdapter(
    private val dateList: List<CalendarDate>,
    selectedDate: String,
    private val onDateClick: (CalendarDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {

    private var selectedPosition = dateList.indexOfFirst { it.fullDate == selectedDate }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: TextView = itemView.findViewById(R.id.txtDay)
        val dot: View = itemView.findViewById(R.id.dot)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedDate = dateList[position]
                    val previousSelected = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
                    onDateClick(clickedDate)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val item = dateList[position]
        holder.txtDay.text = item.day
        holder.txtDate.text = item.date

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_date)
            holder.txtDay.setTextColor(Color.BLACK)
            holder.txtDate.setTextColor(Color.BLACK)
            holder.dot.setBackgroundResource(R.drawable.bg_dot_selected)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.txtDay.setTextColor(Color.WHITE)
            holder.txtDate.setTextColor(Color.WHITE)
            holder.dot.setBackgroundResource(R.drawable.bg_dot)
        }
    }

    override fun getItemCount(): Int = dateList.size
}
