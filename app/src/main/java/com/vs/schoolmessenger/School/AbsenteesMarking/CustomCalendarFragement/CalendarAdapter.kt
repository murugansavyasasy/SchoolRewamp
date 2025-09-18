package com.vs.schoolmessenger.School.AbsenteesMarking.CustomCalendarFragement


import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.time.LocalDate

class CalendarAdapter(
    private val onDateClicked: (LocalDate) -> Unit,
    private val minDate: LocalDate?,
    private val maxDate: LocalDate?
) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {

    private var days: List<LocalDate?> = emptyList()
    private var selectedDate: LocalDate? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private var today: LocalDate = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitList(newDays: List<LocalDate?>, selected: LocalDate?, current: LocalDate) {
        days = newDays
        selectedDate = selected
        today = current
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: LocalDate) {
        selectedDate = date
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false)
        return DateViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = days[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = days.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateBox: TextView = itemView.findViewById(R.id.dateBox)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(date: LocalDate?) {
            if (date == null) {
                dateBox.text = ""
                dateBox.setBackgroundResource(0)
                dateBox.setTextColor(Color.TRANSPARENT)
                dateBox.isClickable = false
                return
            }

            dateBox.text = date.dayOfMonth.toString()
            dateBox.setTextColor(Color.BLACK)
            dateBox.isClickable = true

            // Handle disabled state
            val isBeforeMin = minDate != null && date.isBefore(minDate)
            val isAfterMax = maxDate != null && date.isAfter(maxDate)
            val isSunday = date.dayOfWeek == java.time.DayOfWeek.SUNDAY //Disabling the sunday
            val isDisabled = isBeforeMin || isAfterMax||isSunday

            if (isDisabled) {
                when {
                    isSunday -> {
                        // Sunday → red text, no click
                        dateBox.setTextColor(Color.RED)
                        dateBox.setBackgroundResource(0)
                        dateBox.isClickable = false
                    }
                    else -> {
                        // Other disabled dates → light gray
                        dateBox.setTextColor(Color.LTGRAY)
                        dateBox.setBackgroundResource(0)
                        dateBox.isClickable = false
                    }
                }
            } else {
                when {
                    selectedDate == date -> {
                        dateBox.setBackgroundResource(R.drawable.bg_today_primary)
                        dateBox.setTextColor(Color.WHITE)
                    }
                    date == today -> {
                        dateBox.setBackgroundResource(R.drawable.light_primary_selected_today)
                        dateBox.setTextColor(Color.WHITE)
                    }
                    else -> {
                        dateBox.setBackgroundResource(0)
                        dateBox.setTextColor(Color.BLACK)
                    }
                }

                dateBox.setOnClickListener {
                    onDateClicked(date)
                }
            }
        }
    }
}
