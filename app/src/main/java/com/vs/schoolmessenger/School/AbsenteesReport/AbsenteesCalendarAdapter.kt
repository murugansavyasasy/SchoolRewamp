package com.vs.schoolmessenger.School.AbsenteesReport

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

class AbsenteesCalendarAdapter(
    private val daysInMonth: List<Pair<String, Int>>,
    private val month: Int,
    private val year: Int,
    private val onDateClick: (String) -> Unit
) : BaseAdapter() {


    private var selectedPosition: Int = -1

    init {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
        selectedPosition = daysInMonth.indexOfFirst { it.first == today && it.second != -1 }
    }

    override fun getCount(): Int = daysInMonth.size
    override fun getItem(position: Int): Any = daysInMonth[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_day_absentees, parent, false)

        val dayText = view.findViewById<TextView>(R.id.btnDay)
        val (day, isCurrentMonth) = daysInMonth[position]

        dayText.text = day

        if (isCurrentMonth == -1 || day.isEmpty()) {
            dayText.setTextColor(Color.GRAY)
        } else {
            dayText.setTextColor(Color.BLACK)
        }


        if (position == selectedPosition) {
            dayText.setBackgroundResource(R.drawable.day_selected_absentees)
            dayText.setTextColor(Color.WHITE)
        } else {
            dayText.setBackgroundResource(R.drawable.day_unselected)
            dayText.setTextColor(Color.BLACK)
        }


        dayText.alpha = if (isCurrentMonth != -1) 1f else 0.3f

        view.setOnClickListener {
            if (day.isNotEmpty()) {
                selectedPosition = position
                notifyDataSetChanged()

                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day.toInt())

                val fullDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
                onDateClick(fullDate)
            }
        }


        return view
    }
}
