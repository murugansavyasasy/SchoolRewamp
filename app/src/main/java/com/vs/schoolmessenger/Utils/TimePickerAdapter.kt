package com.vs.schoolmessenger.Utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class TimePickerAdapter(
    private val values: List<String>,
    private val onItemSelected: (Int) -> Unit,
    private var selectedItem: Int // Pass the selected item index to highlight it
) : RecyclerView.Adapter<TimePickerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.time_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = values[position]
        holder.textView.textSize = 18f
        holder.textView.setTextColor(
            if (position == selectedItem) {
                ContextCompat.getColor(holder.itemView.context, R.color.dark_blue_and_green_) // Retrieve the color value
            } else {
                ContextCompat.getColor(holder.itemView.context, R.color.black)
            }
        )


        holder.textView.setOnClickListener {
            val previousItem = selectedItem
            selectedItem = position
            notifyItemChanged(previousItem)
            notifyItemChanged(selectedItem)
            onItemSelected(position) // Pass the index directly
        }
    }

    override fun getItemCount(): Int = values.size
}



