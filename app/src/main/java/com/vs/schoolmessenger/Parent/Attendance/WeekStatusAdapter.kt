package com.vs.schoolmessenger.Parent.Attendance

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.Attendance.WeekStatusModel.GetWeekStatusData
import com.vs.schoolmessenger.R

class WeekStatusAdapter(private val items: List<GetWeekStatusData>) :
    RecyclerView.Adapter<WeekStatusAdapter.WeekStatusViewHolder>() {

    inner class WeekStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        val imgTick: ImageView = itemView.findViewById(R.id.imgTick)
        val lbltext: TextView = itemView.findViewById(R.id.lbltext)
        val lnrBackground: LinearLayout = itemView.findViewById(R.id.lnrBackground)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekStatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.week_status_item, parent, false)
        return WeekStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeekStatusViewHolder, position: Int) {
        val item = items[position]
        val status = item.status.trim()

        holder.lblDay.text = item.day

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            when (status) {
                "P" -> {
                    setColor(ContextCompat.getColor(holder.itemView.context, R.color.PrimaryColor))
                    setStroke(0, Color.TRANSPARENT)
                    holder.imgTick.visibility = View.VISIBLE
                    holder.lbltext.visibility = View.GONE
                }
                "A" -> {
                    setColor(Color.RED)
                    setStroke(0, Color.TRANSPARENT)
                    holder.imgTick.visibility = View.GONE
                    holder.lbltext.visibility = View.VISIBLE
                    holder.lbltext.text = "A"
                }
                else -> {
                    setColor(Color.WHITE)
                    setStroke(2, Color.RED)
                    holder.imgTick.visibility = View.GONE
                    holder.lbltext.visibility = View.GONE
                }
            }
        }

        holder.lnrBackground.background = drawable
    }

    override fun getItemCount(): Int = items.size
}
