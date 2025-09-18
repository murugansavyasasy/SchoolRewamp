package com.vs.schoolmessenger.School.AbsenteesReport

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AbsenteesCustomCalender(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val tvMonthYear: TextView
    private val gridCalendar: GridView
    private val gridWeekdays: GridView
    private val btnPrevMonth: ImageView
    private val btnNextMonth: ImageView
    private val linearlayout: LinearLayout
    private val calendar = Calendar.getInstance()

    private var onDateSelectedListener: ((String) -> Unit)? = null
    fun setOnDateSelectedListener(listener: (String) -> Unit) {
        onDateSelectedListener = listener
    }


    init {
        LayoutInflater.from(context).inflate(R.layout.custom_calendar, this, true)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        gridCalendar = findViewById(R.id.gridCalendar)
        gridWeekdays = findViewById(R.id.gridWeekdays)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        linearlayout = findViewById(R.id.linear_layout)

        linearlayout.visibility = View.GONE

        // Weekdays header
        val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        gridWeekdays.adapter = ArrayAdapter(
            context,
            R.layout.item_weekday,
            R.id.tvWeekday,
            weekdays
        )

        setupCalendar()

        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }
        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            setupCalendar()
        }
    }

    private fun setupCalendar() {
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = monthYear.format(calendar.time)

        val daysInMonth = ArrayList<Pair<String, Int>>()
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        for (i in 1 until firstDayOfWeek) {
            daysInMonth.add(Pair("", -1))
        }

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) {
            tempCal.set(Calendar.DAY_OF_MONTH, i)
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            daysInMonth.add(Pair(i.toString(), dayOfWeek))
        }

        // Simple adapter - just click and return date
        val adapter = AbsenteesCalendarAdapter(daysInMonth) { selectedDate ->
            onDateSelectedListener?.invoke(selectedDate)
        }
        gridCalendar.adapter = adapter
    }
}
