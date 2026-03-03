package com.vs.schoolmessenger.School.StudentDetails.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R

class ChartLegendAdapter(
    private val items: List<Pair<String, Int>>
) : RecyclerView.Adapter<ChartLegendAdapter.ViewHolder>() {

    private val colors = listOf(
        Color.parseColor("#3F51B5"),
        Color.parseColor("#E91E63"),
        Color.parseColor("#4CAF50"),
        Color.parseColor("#FF9800"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#009688"),
        Color.parseColor("#F44336")
    )

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chart_legend, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val subject = items[position].first
        val mark = items[position].second

        val colorView = holder.itemView.findViewById<View>(R.id.viewColor)
        val tvLegend = holder.itemView.findViewById<TextView>(R.id.tvLegend)

        val barColor = colors[position % colors.size]
        colorView.background.setTint(barColor)
        val shortName = getShortName(subject)

        tvLegend.text = "$shortName : ${getGrade(mark)}"
    }

    // Same abbreviation logic as chart
    private fun getShortName(subject: String): String {
        if (subject.length <= 10) return subject

        val words = subject.trim().split(" ")
        return if (words.size > 1) {
            words.joinToString("") { it.first().uppercase() }
        } else {
            subject.take(3).uppercase()
        }
    }

    private fun getGrade(mark: Int): String {
        return when {
            mark >= 90 -> "A+"
            mark >= 80 -> "A"
            mark >= 70 -> "B+"
            mark >= 60 -> "B"
            mark >= 50 -> "C"
            else -> "D"
        }
    }
}