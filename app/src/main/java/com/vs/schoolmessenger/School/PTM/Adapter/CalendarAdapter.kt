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
    private val days: List<Pair<String, Int>>, // day string + dayOfWeek
    private val selectedDates: ArrayList<String>, // stores full date "dd-MM-yyyy"
    private val calendar: Calendar // month being shown
) : BaseAdapter() {

    private val today = Calendar.getInstance()
    private val todayStr: String =
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(today.time)

    override fun getCount(): Int = days.size

    override fun getItem(position: Int): Any = days[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_day, parent, false)

        val tvDay = view.findViewById<TextView>(R.id.btnDay)
        val (day, _) = days[position]

        if (day.isEmpty()) {
            tvDay.text = ""
            tvDay.setBackgroundColor(Color.TRANSPARENT)
        } else {
            tvDay.text = day

            // Reset style
            tvDay.setTextColor(Color.BLACK)
            tvDay.setBackgroundColor(Color.TRANSPARENT)

            // Build full date string for this cell
            val cellCal = calendar.clone() as Calendar
            cellCal.set(Calendar.DAY_OF_MONTH, day.toInt())
            val cellDateStr =
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cellCal.time)

            // Highlight today
            when {
                cellDateStr == todayStr -> {
                    tvDay.setBackgroundResource(R.drawable.bg_circle_selecto)
                    tvDay.setTextColor(Color.WHITE)
                }
                selectedDates.contains(cellDateStr) -> {
                    tvDay.setBackgroundResource(R.drawable.bg_selected_day)
                    tvDay.setTextColor(Color.WHITE)
                }
                else -> {
                    tvDay.setBackgroundResource(android.R.color.transparent)
                    tvDay.setTextColor(Color.BLACK)
                }
            }

        }
        return view
    }
}
