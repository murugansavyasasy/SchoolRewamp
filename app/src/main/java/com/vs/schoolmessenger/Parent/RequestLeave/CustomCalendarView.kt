package com.vs.schoolmessenger.Parent.RequestLeave

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val tvMonthYear: TextView
    private val rvCalendarDays: RecyclerView

    private var selectedDate: Date? = null
    private val calendar = Calendar.getInstance()
    private var days = mutableListOf<CalendarDay>()

    var onDateSelected: ((Date) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_custom_calendar, this, true)
        orientation = VERTICAL

        tvMonthYear = findViewById(R.id.tvMonthYear)
        rvCalendarDays = findViewById(R.id.rvCalendarDays)

        rvCalendarDays.layoutManager = GridLayoutManager(context, 7)
        generateCalendar(calendar)
    }

    private fun generateCalendar(base: Calendar) {
        days.clear()

        val currentMonth = base.get(Calendar.MONTH)
        val currentYear = base.get(Calendar.YEAR)

        val tempCal = Calendar.getInstance()
        tempCal.set(currentYear, currentMonth, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1

        tempCal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        val today = Date()
        var defaultSelected: CalendarDay? = null

        for (i in 0 until 42) {
            val dayDate = tempCal.time
            val isCurrentMonth = tempCal.get(Calendar.MONTH) == currentMonth
            val isSelected = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(dayDate) ==
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(today)

            val day = CalendarDay(dayDate, isCurrentMonth, isSelected)
            if (isSelected) {
                defaultSelected = day
                selectedDate = dayDate
            }

            days.add(day)
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        tvMonthYear.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(base.time)

        rvCalendarDays.adapter = CalendarAdapter(days) { selected ->
            days.forEach { it.isSelected = false }
            selected.isSelected = true
            selectedDate = selected.date
            rvCalendarDays.adapter?.notifyDataSetChanged()
            onDateSelected?.invoke(selected.date)
        }

        // Callback once on launch if needed
        defaultSelected?.let {
            onDateSelected?.invoke(it.date)
        }
    }


    fun setDate(date: Date) {
        calendar.time = date
        generateCalendar(calendar)
    }

    fun getSelectedDate(): Date? = selectedDate
}
