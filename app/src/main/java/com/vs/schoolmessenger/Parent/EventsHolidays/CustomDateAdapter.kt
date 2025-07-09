package com.vs.schoolmessenger.Parent.EventsHolidays

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.EventsHolidays.HolidayActivity.Model.Holiday
import com.vs.schoolmessenger.R
import kotlin.collections.contains

class CustomDateAdapter(
    private val context: Context,
    private val onDateClick: (List<String>) -> Unit,
    private var holidays: List<Holiday>,
    private val isSelectionEnabled: Boolean = true // Optional flag
) : RecyclerView.Adapter<CustomDateAdapter.DateViewHolder>() {

    private val dates = mutableListOf<CustomDateItem>()
    private val selectedDates = mutableSetOf<String>()
    private val selectedBackgroundDrawable = ContextCompat.getDrawable(context, R.drawable.rect_round_light_green)

    fun submitDates(newDates: List<CustomDateItem>) {
        dates.clear()
        dates.addAll(newDates)
        notifyDataSetChanged()
        Log.d("CustomDateAdapter", "Dates submitted: $dates")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_box, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val dateItem = dates[position]
        holder.bind(dateItem)
    }

    override fun getItemCount(): Int = dates.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateBox: TextView = itemView.findViewById(R.id.dateBox)

        fun bind(dateItem: CustomDateItem) {
            if (dateItem.day == null) {
                dateBox.text = ""
                dateBox.isClickable = false
                dateBox.setBackgroundResource(0)
                return
            }

            dateBox.text = dateItem.day.toString()
            dateBox.isClickable = isSelectionEnabled && dateItem.isSelectable
            dateBox.setBackgroundColor(Color.TRANSPARENT)


            dateBox.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (dateItem.isSelectable) R.color.colorPrimary else R.color.grey
                )
            )


            val dateStr = dateItem.getFormattedDate()
            if (dateItem.isHoliday) {
                dateBox.background = ContextCompat.getDrawable(context, R.drawable.ic_holiday_dot)
                dateBox.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                dateBox.background = null
                dateBox.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (dateItem.isSelectable) R.color.colorPrimary else R.color.black
                    )
                )
            }



            if (isSelectionEnabled && dateItem.isSelectable) {
                dateBox.setOnClickListener {
                    if (dateStr != null) {
                        if (selectedDates.contains(dateStr)) {
                            selectedDates.remove(dateStr)
                            dateBox.setBackgroundColor(Color.TRANSPARENT)
                        } else {
                            selectedDates.add(dateStr)
                            dateBox.background = selectedBackgroundDrawable
                        }
                        onDateClick(selectedDates.toList())
                    }
                }


                if (selectedDates.contains(dateStr)) {
                    dateBox.background = selectedBackgroundDrawable
                } else {
                    dateBox.setBackgroundColor(Color.TRANSPARENT)
                }
            } else {
                dateBox.setOnClickListener(null)
            }
        }
    }


}


