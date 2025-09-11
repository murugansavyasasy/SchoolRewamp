package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
import com.vs.schoolmessenger.R

class ParentSlotTimingAdapter(
    private val slots: List<SlotData>,
    private val onSlotClick: (SlotData) -> Unit
) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
        val tvSlotStatus: TextView = itemView.findViewById(R.id.tvSlotStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slot_timing_item, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]

        holder.tvSlotTime.text = "${slot.slot_from} - ${slot.slot_to}"
        holder.tvSlotStatus.text = if (slot.is_booked) {
            if (slot.my_booking) "Booked by You" else "Booked"
        } else {
            "Available"
        }

        holder.itemView.setOnClickListener {
            if (!slot.is_booked) {
                onSlotClick(slot)
            }
        }
    }

    override fun getItemCount(): Int = slots.size
}
