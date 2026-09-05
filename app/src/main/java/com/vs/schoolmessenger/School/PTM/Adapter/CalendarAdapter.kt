package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarAdapter(
    private val context: Context,
    private val days: List<Pair<String, Int>>, // (day, dayOfWeek)
    private val selectedDates: ArrayList<String>, // full date "dd-MM-yyyy"
    private val calendar: Calendar, // current visible month
    private val minDate: Long // min selectable date in millis
) : BaseAdapter() {

    private val today = Calendar.getInstance()
    private val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(today.time)

    override fun getCount(): Int = days.size
    override fun getItem(position: Int): Any = days[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_day, parent, false)

        val tvDay = view.findViewById<TextView>(R.id.btnDay)
        val (day, dayOfWeek) = days[position]

        if (day.isEmpty()) {
            tvDay.text = ""
            tvDay.setBackgroundColor(Color.TRANSPARENT)
            tvDay.isEnabled = false
            return view
        }

        tvDay.text = day

        val cellCal = calendar.clone() as Calendar
        cellCal.set(Calendar.DAY_OF_MONTH, day.toInt())
        val cellDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(cellCal.time)

        // 🔹 Disable days before minDate
        if (cellCal.timeInMillis < minDate) {
            tvDay.isEnabled = false
            tvDay.setTextColor(Color.LTGRAY)
            tvDay.setBackgroundColor(Color.TRANSPARENT)
            return view
        }

        // 🔹 Default style
        tvDay.isEnabled = true
        tvDay.setBackgroundColor(Color.TRANSPARENT)
        tvDay.setTextColor(Color.BLACK)

        // 🔹 Sunday = Red text color
        if (dayOfWeek == Calendar.SUNDAY) {
            tvDay.setTextColor(Color.RED)
        }

        // 🔹 Apply styling
        when {
            selectedDates.contains(cellDateStr) -> {
                tvDay.setBackgroundResource(R.drawable.bg_selected_day)
                tvDay.setTextColor(Color.WHITE)
            }

            cellDateStr == todayStr -> {
                tvDay.setBackgroundResource(R.drawable.circle_bg_primary)
                tvDay.setTextColor(Color.WHITE)
            }
        }

        return view
    }
}
