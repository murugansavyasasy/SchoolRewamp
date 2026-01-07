package com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale

class HolidayAdapter(
    private val holidays: List<Holiday>
) : RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder>() {

    class HolidayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtHolidayName)
        val txtDate: TextView = itemView.findViewById(R.id.txtHolidayDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_holiday, parent, false)
        return HolidayViewHolder(view)
    }

    override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
        val holiday = holidays[position]
        holder.txtName.text = holiday.name

        // Format date to "dd MMM yyyy"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.txtDate.text = try {
            val parsedDate = inputFormat.parse(holiday.date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            holiday.date
        }
    }

    override fun getItemCount(): Int = holidays.size
}

