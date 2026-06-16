package com.vs.schoolmessenger.School.Communication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant

class SelectedDatesAdapter(
    private val context: Context,
    private val selectedDates: MutableList<String>,
    private val dateAdapter: DateAdapter,
    private val onDateRemoved: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = selectedDates.size
    override fun getItem(position: Int): String = selectedDates[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun submitSelectedDates(newSelectedDates: List<String>) {
        selectedDates.clear()
        selectedDates.addAll(newSelectedDates)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.schedule_call_date_data, parent, false)

        val dateTextView: TextView = view.findViewById(R.id.lblScheduleDate)
        val imgRemoveDate: ImageView = view.findViewById(R.id.imgRemoveDate)
        val date = selectedDates[position]
        dateTextView.text = Constant.convertDateTimeFormat(date)

        imgRemoveDate.setOnClickListener {
            val removedDate = selectedDates[position]
            selectedDates.removeAt(position)
            notifyDataSetChanged()
            onDateRemoved(removedDate)
        }
        return view
    }
}
