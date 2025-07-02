package com.vs.schoolmessenger.Parent.InteractionWithStaff

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
class YearAdapter(
    private val years: List<Int>,
    private val selectedYear: Int,
    private val onYearClick: (Int) -> Unit
) : RecyclerView.Adapter<YearAdapter.YearViewHolder>() {


    inner class YearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yearText: TextView = itemView.findViewById(R.id.tvYear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_year, parent, false)
        return YearViewHolder(view)
    }

    override fun getItemCount(): Int = years.size

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        val year = years[position]
        holder.yearText.text = year.toString()

        if (year == selectedYear) {
            holder.yearText.setBackgroundResource(R.color.gnt_blue)
            holder.yearText.setTextColor(Color.WHITE)
        } else {
            holder.yearText.setBackgroundColor(Color.TRANSPARENT)
            holder.yearText.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            onYearClick(year)
        }
    }
}