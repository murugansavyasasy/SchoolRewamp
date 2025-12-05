package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotCountData
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PtmParentCalender(
    private val dates: List<Triple<String, Int, Int>>, // month, day, year
    private val slotCounts: List<SlotCountData>,
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<PtmParentCalender.DateViewHolder>() {
    private var selectedPos = -1

    inner class DateViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvMonth: TextView = view.findViewById(R.id.tvMonth)
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val itemRoot: LinearLayout = view.findViewById(R.id.itemRoot)
        val lblSlotCount: TextView = view.findViewById(R.id.lblSlotCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_ptm_calender_item, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val (month, day, year) = dates[position]

        holder.tvMonth.text = month
        holder.tvDay.text = day.toString()

        // Highlight selected
        val isSelected = selectedPos == position
        holder.itemView.isSelected = isSelected
        holder.tvMonth.setTextColor(if (isSelected) Color.WHITE else Color.BLACK)

        // Format this calendar item date
        val formattedDate = formatDate(month, day, year) // dd-MM-yyyy

        // Find if this date has a slot count
        val countData = slotCounts.find { it.event_date == formattedDate }
        if (countData != null && countData.count != "0") {
            holder.lblSlotCount.visibility = View.VISIBLE
            (holder.lblSlotCount as TextView).text = countData.count
        } else {
//            holder.lblSlotCount.visibility = View.GONE
//            holder.lblSlotCount.visibility = View.INVISIBLE
            holder.lblSlotCount.text = ""
        }

        // Handle click
        holder.itemRoot.setOnClickListener {
            val prevPos = selectedPos
            selectedPos = position
            notifyItemChanged(prevPos)
            notifyItemChanged(selectedPos)
            notifyDataSetChanged()
            onDateClick(formattedDate)
        }
    }


    override fun getItemCount() = dates.size

    /** Set default selected date by position */
    fun setDefaultSelected(pos: Int) {
        if (pos in dates.indices) {
            selectedPos = pos
            notifyItemChanged(selectedPos)

            val (month, day, year) = dates[pos]
            val formattedDate = formatDate(month, day, year)
            onDateClick(formattedDate)
        }
    }


    /** Optionally select today’s date if it exists in the list */
    fun selectToday() {
        val today = Calendar.getInstance()
        val todayPos = dates.indexOfFirst { (_, day) ->
            day == today.get(Calendar.DAY_OF_MONTH)
        }
        if (todayPos != -1) setDefaultSelected(todayPos)
    }

    /** Helper: convert month string to index */
    private fun monthToIndex(month: String): Int {
        return when (month.lowercase(Locale.ENGLISH)) {
            "jan", "january" -> 0
            "feb", "february" -> 1
            "mar", "march" -> 2
            "apr", "april" -> 3
            "may" -> 4
            "jun", "june" -> 5
            "jul", "july" -> 6
            "aug", "august" -> 7
            "sep", "sept", "september" -> 8
            "oct", "october" -> 9
            "nov", "november" -> 10
            "dec", "december" -> 11
            else -> 0
        }
    }

    private fun formatDate(month: String, day: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthToIndex(month))
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return outputFormat.format(calendar.time)
    }
}
