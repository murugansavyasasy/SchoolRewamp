package com.vs.schoolmessenger.School.Communication.Adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Communication.DataClass.DateItem

class DateAdapter(
    private val context: Context,
    private val onDateClick: (List<String>) -> Unit // Lambda to handle multiple selections
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<DateItem>()
    private val selectedDates = mutableSetOf<String>() // Set to track selected dates
    private val selectedBackgroundDrawable =
        ContextCompat.getDrawable(context, R.drawable.rect_round_light_green)

    fun submitDates(newDates: List<DateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
        Log.d("DateAdapter", "Dates submitted: $dates")
    }
    fun removeSelectedDate(dateStr: String) {
        if (selectedDates.contains(dateStr)) {
            selectedDates.remove(dateStr)
            notifyDataSetChanged()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_date_box, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        holder.bind(dateItem)
    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateBox: TextView = itemView.findViewById(R.id.dateBox)

        fun bind(dateItem: DateItem) {
            if (dateItem.day == null) {
                dateBox.text = ""
                dateBox.isClickable = false
                dateBox.setBackgroundResource(0)
                return
            }

            dateBox.text = dateItem.day.toString()
            dateBox.isClickable = dateItem.isSelectable
            dateBox.setBackgroundColor(Color.TRANSPARENT)

            if (dateItem.isSelectable) {
                dateBox.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

                dateBox.setOnClickListener {
                    val dateStr = dateItem.getFormattedDate() ?: return@setOnClickListener
                    if (selectedDates.contains(dateStr)) {
                        selectedDates.remove(dateStr)
                        dateBox.setBackgroundColor(Color.TRANSPARENT)
                    } else {
                        selectedDates.add(dateStr)
                        dateBox.background = selectedBackgroundDrawable
                    }
                    onDateClick(selectedDates.toList())
                }

                if (selectedDates.contains(dateItem.getFormattedDate())) {
                    dateBox.background = selectedBackgroundDrawable
                }
            } else {
                dateBox.setTextColor(ContextCompat.getColor(context, R.color.grey))
                dateBox.setBackgroundColor(Color.TRANSPARENT)
                dateBox.setOnClickListener(null)
            }
        }
    }

    fun setSelectedDates(selectedDates: List<String>) {
        this.selectedDates.clear()
        this.selectedDates.addAll(selectedDates)
        Log.d("DateAdapter", "Selected dates set: $selectedDates")
        notifyDataSetChanged() // Notify that data has changed
    }

    fun getSelectedDates(): List<String> {
        return selectedDates.toList() // Ensure this returns a List<String>
    }
}
