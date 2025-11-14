package com.vs.schoolmessenger.School.AbsenteesReport

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

class CalendarAbsenteesAdapter(
    private val onDateClicked: (LocalDate) -> Unit,
    private val minDate: LocalDate?,
    private val maxDate: LocalDate?,
    private val isAbsenteesReport: Boolean = false
) : RecyclerView.Adapter<CalendarAbsenteesAdapter.DateViewHolder>() {

    private var days: List<LocalDate?> = emptyList()
    private var selectedDate: LocalDate? = null
    private var absentDates: Set<LocalDate> = emptySet()

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

    fun setAbsentDates(dates: Set<LocalDate>) {
        absentDates = dates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val layoutId = if (isAbsenteesReport) {
            R.layout.calendar_day_item_absentees_red
        } else {
            R.layout.calendar_day_item
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
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
        private val dotIndicator: View? = itemView.findViewById(R.id.dot_indicator)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(date: LocalDate?) {
            dotIndicator?.visibility = View.GONE

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

            val isBeforeMin = minDate != null && date.isBefore(minDate)
            val isAfterMax = maxDate != null && date.isAfter(maxDate)
            val isSunday = date.dayOfWeek == java.time.DayOfWeek.SUNDAY
            val isDisabled = isBeforeMin || isAfterMax

            if (isDisabled) {
                // Only min/max restricted dates are disabled
                dateBox.setTextColor(Color.LTGRAY)
                dateBox.setBackgroundResource(0)
                dateBox.isClickable = false
            } else {
                // Sundays are clickable and shown in red
                if (isSunday) {
                    dateBox.setTextColor(Color.RED)
                }

                when {
                    selectedDate == date -> {
                        dateBox.setBackgroundResource(R.drawable.bg_today_red)
                        dateBox.setTextColor(Color.WHITE)
                    }
                    date == today -> {
                        dateBox.setBackgroundResource(R.drawable.light_red_selected_today)
                        dateBox.setTextColor(Color.WHITE)
                    }
                    else -> {
                        if (!isSunday) {
                            dateBox.setTextColor(Color.BLACK)
                        }
                        dateBox.setBackgroundResource(0)
                    }
                }

                dateBox.setOnClickListener {
                    onDateClicked(date)
                }
            }

            // Dot for absentees (only for report mode)
            if (isAbsenteesReport && !isDisabled && absentDates.contains(date)) {
                dotIndicator?.visibility = View.VISIBLE
                dotIndicator?.setBackgroundColor(Color.RED)
            }
        }
    }
}
