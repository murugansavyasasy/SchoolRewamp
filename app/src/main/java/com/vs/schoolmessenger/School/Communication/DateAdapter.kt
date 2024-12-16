package com.vs.schoolmessenger.School.Communication

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

class DateAdapter(
    private val context: Context,
    private val onDateClick: (List<String>) -> Unit // Lambda to handle multiple selections
) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<DateItem>()
    private val selectedDates = mutableSetOf<String>() // Set to track selected dates
    private val selectedBackgroundDrawable =
        ContextCompat.getDrawable(context, R.drawable.rect_shadow_green)

    fun submitDates(newDates: List<DateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
        Log.d("DateAdapter", "Dates submitted: $dates")
    }

    fun removeSelectedDate(date: String) {
        if (selectedDates.contains(date)) {
            selectedDates.remove(date)
            notifyDataSetChanged()
            Log.d("DateAdapter", "Removed selected date: $date. Current selected dates: $selectedDates")
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
                // Empty cell
                dateBox.text = ""
                dateBox.isClickable = false
                dateBox.setBackgroundResource(0)
            } else {
                // Display day only
                dateBox.text = dateItem.day.toString() // Display just the day
                dateBox.isClickable = dateItem.isSelectable

                // Reset background color for all items
                dateBox.setBackgroundColor(Color.TRANSPARENT)

                // Style based on whether the date is selectable
                if (dateItem.isSelectable) {
                    dateBox.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    dateBox.setOnClickListener {
                        val dateStr = dateItem.getFormattedDate() ?: return@setOnClickListener
                        if (selectedDates.contains(dateStr)) {
                            // Deselect the date
                            selectedDates.remove(dateStr)
                            Log.d("DateAdapter", "Deselected date: $dateStr. Current selected dates: $selectedDates")
                            dateBox.setBackgroundColor(Color.TRANSPARENT) // Reset background
                        } else {
                            // Select the date
                            selectedDates.add(dateStr)
                            Log.d("DateAdapter", "Selected date: $dateStr. Current selected dates: $selectedDates")
                            dateBox.background = selectedBackgroundDrawable // Set selected color
                        }
                        // Notify the listener about the current selection
                        onDateClick(selectedDates.toList())
                    }

                    // Change background if this date is selected
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
    }


    fun selectDate(date: String) {
        selectedDates.add(date)
        Log.d("DateAdapter", "Selected date added: $date. Current selected dates: $selectedDates")
        notifyDataSetChanged()
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
