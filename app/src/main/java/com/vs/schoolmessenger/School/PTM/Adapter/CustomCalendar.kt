package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
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
    private val btnPrevMonth: ImageView
    private val btnNextMonth: ImageView

    private val btnCancel: Button
    private val calendar = Calendar.getInstance()
    private val selectedDates = ArrayList<String>()
    private var adapter: CalendarAdapter? = null

    private var onCancelListener: (() -> Unit)? = null
    fun setOnCancelListener(listener: () -> Unit) {
        onCancelListener = listener
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_calendar, this, true)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        gridCalendar = findViewById(R.id.gridCalendar)
        gridWeekdays = findViewById(R.id.gridWeekdays)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnCancel = findViewById(R.id.btnCancelCalendar)

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

        btnCancel.setOnClickListener {
            selectedDates.clear()
            adapter?.notifyDataSetChanged()
            onCancelListener?.invoke()
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
                    selectedDates.add(fullDate)
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
