package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomCalendar(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private val tvMonthYear: TextView
    private val gridCalendar: GridView
    private val gridWeekdays: GridView
    private val btnPrevMonth: TextView
    private val btnNextMonth: TextView
    private val calendar = Calendar.getInstance()
    private val selectedDates = ArrayList<String>() // now stores "dd-MM-yyyy"
    private var adapter: CalendarAdapter? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_calendar, this, true)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        gridCalendar = findViewById(R.id.gridCalendar)
        gridWeekdays = findViewById(R.id.gridWeekdays)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)

        // Weekdays row
        val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        gridWeekdays.adapter = object : ArrayAdapter<String>(
            context,
            R.layout.item_weekday,
            R.id.tvWeekday,
            weekdays
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                if (position == 0) {
                    view.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
                return view
            }
        }

        setupCalendar()

        // Month navigation
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

        // Find which day of week 1st starts (1=Sunday, 7=Saturday)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)

        // Add empty slots before the 1st date
        for (i in 1 until firstDayOfWeek) {
            daysInMonth.add(Pair("", -1))
        }

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDay) {
            tempCal.set(Calendar.DAY_OF_MONTH, i)
            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            daysInMonth.add(Pair(i.toString(), dayOfWeek))
        }

        adapter = CalendarAdapter(context, daysInMonth, selectedDates, calendar)
        gridCalendar.adapter = adapter

        gridCalendar.setOnItemClickListener { _, _, pos, _ ->
            val (day, _) = daysInMonth[pos]
            if (day.isNotEmpty()) {
                val tempCal2 = calendar.clone() as Calendar
                tempCal2.set(Calendar.DAY_OF_MONTH, day.toInt())
                val fullDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(tempCal2.time)

                if (selectedDates.contains(fullDate)) {
                    selectedDates.remove(fullDate) // unselect
                } else {
                    selectedDates.add(fullDate) // select
                }
                adapter?.notifyDataSetChanged()
            }
        }
    }

    fun getSelectedDates(): ArrayList<String> = selectedDates

    fun setSelectedDates(list: ArrayList<String>) {
        selectedDates.clear()
        selectedDates.addAll(list)
        adapter?.notifyDataSetChanged()
    }
}
