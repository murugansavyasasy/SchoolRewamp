package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale

class ParentSlotTimingAdapter(
    private val slots: List<SlotData>,
    private val allSlots: List<SlotData>,
    private val allSelectedSlots: List<SlotData>,
    private val onSlotClick: (SlotData) -> Unit
) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {

    private var selectedSlot: SlotData? = null
    private var myBookedSlot: SlotData? = null

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
        val tvSlotStatus: TextView = itemView.findViewById(R.id.tvSlotStatus)
        val card: LinearLayout = itemView.findViewById(R.id.lnrHeader)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slot_timing_item, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]

        holder.tvSlotTime.text = "${slot.slot_from} - ${slot.slot_to}"

        val isOverlappingWithAnyBooked = allSlots.any {
            it != slot && (it.is_booked || it.my_booking) && isOverlapping(slot, it)
        }
        val isOverlappingWithSelected = allSelectedSlots.any {
            it != slot && isOverlapping(slot, it)
        }

        when {
            slot.is_booked && !slot.my_booking -> {
                holder.card.setBackgroundResource(R.drawable.bg_light_orange)
                holder.card.isEnabled = false
            }

            slot.my_booking -> {
                holder.card.setBackgroundResource(R.drawable.circle_background_green)
                holder.tvSlotTime.setTextColor(Color.BLACK)
                holder.tvSlotStatus.setTextColor(Color.BLACK)
                holder.card.isEnabled = false
            }

            myBookedSlot != null -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.card.isEnabled = false
            }

            isOverlappingWithAnyBooked -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.card.isEnabled = false
            }

            isOverlappingWithSelected -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.card.isEnabled = false
            }

            selectedSlot == slot -> {
                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
                holder.tvSlotTime.setTextColor(Color.WHITE)
                holder.tvSlotStatus.setTextColor(Color.WHITE)
                holder.card.isEnabled = true
            }

            selectedSlot != null && isOverlapping(slot, selectedSlot!!) -> {

                holder.card.isEnabled = false
            }

            else -> {
                holder.card.setBackgroundColor(Color.WHITE)
                holder.card.setBackgroundResource(R.drawable.outline_gray)
                holder.card.isEnabled = true
            }
        }

        holder.itemView.setOnClickListener {
            if (!slot.is_booked && !slot.my_booking && holder.card.isEnabled) {
                onSlotClick(slot)
            }
        }
    }

    override fun getItemCount() = slots.size

    fun setSelectedSlot(slot: SlotData?) {
        selectedSlot = slot
        notifyDataSetChanged()
    }

    fun setMyBookedSlot(slot: SlotData?) {
        myBookedSlot = slot
        notifyDataSetChanged()
    }

    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val from1 = format.parse(slot1.slot_from.trim())
        val to1 = format.parse(slot1.slot_to.trim())
        val from2 = format.parse(slot2.slot_from.trim())
        val to2 = format.parse(slot2.slot_to.trim())

        if (from1 == null || to1 == null || from2 == null || to2 == null) return false

        return from1 < to2 && from2 < to1
    }
}