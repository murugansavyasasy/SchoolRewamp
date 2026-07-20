package com.vs.schoolmessenger.School.PTM.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale

class SelectedDatesAdapter(
    private val dates: ArrayList<String>,
    private val onRemove: (String) -> Unit
) : RecyclerView.Adapter<SelectedDatesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val btnRemove: ImageView = itemView.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_date, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rawDate = dates[position]

        val formattedDate = try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val parsedDate = inputFormat.parse(rawDate)
            if (parsedDate != null) outputFormat.format(parsedDate) else rawDate
        } catch (e: Exception) {
            rawDate
        }
        Log.d("formattedDate", formattedDate)

        holder.tvDate.text = formattedDate

        holder.btnRemove.setOnClickListener {
            onRemove(rawDate)
        }
    }

    override fun getItemCount(): Int = dates.size
}
