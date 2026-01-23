package com.vs.schoolmessenger.Parent.EventsHolidays

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import java.util.Calendar

class CustomDateAdapter(
    private val context: Context,
    private val onDateClick: (List<String>) -> Unit,
    private var holidays: List<Holiday>,
    private val isSelectionEnabled: Boolean = true
) : RecyclerView.Adapter<CustomDateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<CustomDateItem>()
    private val selectedDates = mutableSetOf<String>()
    private val selectedBackgroundDrawable =
        ContextCompat.getDrawable(context, R.drawable.rect_round_light_green)

    private val todayCalendar = Calendar.getInstance()
    private val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
    private val todayMonth = todayCalendar.get(Calendar.MONTH)
    private val todayYear = todayCalendar.get(Calendar.YEAR)

    fun submitDates(newDates: List<CustomDateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
    }

    fun setDates(newDates: List<CustomDateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_date_box, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        holder.bind(dateItem)
    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateBox: TextView = itemView.findViewById(R.id.dateBox)
        private val linaer_layout: LinearLayout = itemView.findViewById(R.id.linaer_layout)

        fun bind(dateItem: CustomDateItem) {
            if (dateItem.day == null) {
                dateBox.text = ""
                dateBox.isClickable = false
//                dateBox.setBackgroundResource(0)
                linaer_layout.setBackgroundResource(0)
                return
            }

            val dateStr = dateItem.getFormattedDate() // Format: yyyy-MM-dd
            dateBox.text = dateItem.day.toString()
            dateBox.isClickable = isSelectionEnabled && dateItem.isSelectable

//            dateBox.setBackgroundColor(Color.TRANSPARENT)
            linaer_layout.setBackgroundColor(Color.TRANSPARENT)

            val calendar = Calendar.getInstance()
            calendar.set(dateItem.year, dateItem.month - 1, dateItem.day)

            val isToday =
                dateItem.day == todayDay &&
                        dateItem.month - 1 == todayMonth &&
                        dateItem.year == todayYear

            if (dateItem.isHoliday) {
//                dateBox.background = ContextCompat.getDrawable(context, R.drawable.ic_holiday_dot)
                linaer_layout.background = ContextCompat.getDrawable(context, R.drawable.ic_holiday_dot)
                dateBox.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (isToday) {
//                dateBox.background = ContextCompat.getDrawable(context, R.drawable.ic_today_dot)
                linaer_layout.background = ContextCompat.getDrawable(context, R.drawable.ic_today_dot)
                dateBox.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                dateBox.background = null
                dateBox.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (dateItem.isSunday) R.color.red
                        else if (dateItem.isSelectable) R.color.colorPrimary
                        else R.color.grey
                    )
                )
            }

            // Handle selection
            if (isSelectionEnabled && dateItem.isSelectable) {
                dateBox.setOnClickListener {
                    if (dateStr != null) {
                        if (selectedDates.contains(dateStr)) {
                            selectedDates.remove(dateStr)
                            linaer_layout.setBackgroundColor(Color.TRANSPARENT)
                        } else {
                            selectedDates.add(dateStr)
                            linaer_layout.background = selectedBackgroundDrawable
                        }
                        onDateClick(selectedDates.toList())
                    }
                }

                if (selectedDates.contains(dateStr)) {
//                    dateBox.background = selectedBackgroundDrawable
                    linaer_layout.background = selectedBackgroundDrawable
                }
            } else {
                dateBox.setOnClickListener(null)
            }
        }
    }
}



