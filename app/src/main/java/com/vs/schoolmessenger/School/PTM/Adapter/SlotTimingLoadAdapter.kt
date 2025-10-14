package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.SlotAvailability

class SlotTimingLoadAdapter(
    private var slotTimes: MutableList<SlotAvailability>,
    private val context: Context,
    private val onDayUpdate: (List<SlotAvailability>) -> Unit
) : RecyclerView.Adapter<SlotTimingLoadAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lblTiming: TextView = itemView.findViewById(R.id.lblTiming)
        val imgRemove: ImageView = itemView.findViewById(R.id.imgRemove)
        val lblSlotStatus: TextView = itemView.findViewById(R.id.lblSlotStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slot_timing_load_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slotTimes[position]
        holder.lblTiming.text = "${slot.slot_from} - ${slot.slot_to}"
        holder.lblSlotStatus.text = slot.slot_availablity
        if (slot.slot_availablity.equals("Available", true)) {
            holder.imgRemove.visibility = View.VISIBLE
            holder.lblSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.imgRemove.visibility = View.GONE
            holder.lblSlotStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.imgRemove.setOnClickListener {
            slotTimes.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, slotTimes.size)
            onDayUpdate(slotTimes)
        }
    }
    override fun getItemCount(): Int = slotTimes.size
}

