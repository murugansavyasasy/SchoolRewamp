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
    private val onSlotClick: (SlotData) -> Unit
) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {

    private var selectedSlot: SlotData? = null
    private var myBookedSlot: SlotData? = null  // Only this meeting
    private var allBookedSlots: List<SlotData> = emptyList() // All meetings

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
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

        // Determine background & enable state
        when {
            // My booking in this meeting → green
            slot.my_booking -> {
                holder.card.setBackgroundColor(Color.parseColor("#4CAF50"))
                holder.card.isEnabled = false
            }
            // Already booked by others → orange
            slot.is_booked && !slot.my_booking -> {
                holder.card.setBackgroundResource(R.drawable.bg_light_orange)
                holder.card.isEnabled = false
            }
            // If I booked a slot in this meeting → disable all other slots gray
            myBookedSlot != null -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.card.isEnabled = false
            }
            // Overlapping with any booked slot in all meetings → disable gray
            allBookedSlots.any { isOverlapping(slot, it) } -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.card.isEnabled = false
            }
            // User-selected slot → blue
            selectedSlot == slot -> {
                holder.card.setBackgroundColor(Color.BLUE)
                holder.card.isEnabled = true
            }
            else -> {
                holder.card.setBackgroundColor(Color.WHITE)
                holder.card.isEnabled = true
            }
        }

        // Click listener
        holder.itemView.setOnClickListener {
            if (holder.card.isEnabled) {
                selectedSlot = slot
                notifyDataSetChanged()
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

    fun setAllBookedSlots(slots: List<SlotData>) {
        allBookedSlots = slots
        notifyDataSetChanged()
    }

    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val from1 = format.parse(slot1.slot_from)
        val to1 = format.parse(slot1.slot_to)
        val from2 = format.parse(slot2.slot_from)
        val to2 = format.parse(slot2.slot_to)

        return from1 < to2 && from2 < to1
    }
}

//
//
//class ParentSlotTimingAdapter(
//    private val slots: List<SlotData>,
//    private val onSlotClick: (SlotData) -> Unit
//) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {
//
//    private var selectedSlot: SlotData? = null
//    private var myBookedSlot: SlotData? = null  // only for this meeting
//
//    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
//        val card: LinearLayout = itemView.findViewById(R.id.lnrHeader)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.slot_timing_item, parent, false)
//        return SlotViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
//        val slot = slots[position]
//
//        holder.tvSlotTime.text = "${slot.slot_from} - ${slot.slot_to}"
//
//        when {
//            // Already booked by others
//            slot.is_booked && !slot.my_booking -> {
//                holder.card.setBackgroundResource(R.drawable.bg_light_orange)
//                holder.card.isEnabled = false
//            }
//            // My booking in this meeting
//            slot.my_booking -> {
//                holder.card.setBackgroundColor(Color.parseColor("#4CAF50")) // green
//                holder.card.isEnabled = false
//            }
//            // If I booked a slot in this meeting → disable all other slots in same meeting
//            myBookedSlot != null -> {
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.card.isEnabled = false
//            }
//            // User-selected slot
//            selectedSlot == slot -> {
//                holder.card.setBackgroundColor(Color.BLUE)
//                holder.card.isEnabled = true
//            }
//            // Overlapping with selected slot
//            selectedSlot != null && isOverlapping(slot, selectedSlot!!) -> {
//                holder.card.setBackgroundColor(Color.RED)
//                holder.card.isEnabled = false
//            }
//            else -> {
//                holder.card.setBackgroundColor(Color.WHITE)
//                holder.card.isEnabled = true
//            }
//        }
//
//        holder.itemView.setOnClickListener {
//            if (!slot.is_booked && !slot.my_booking && holder.card.isEnabled) {
//                onSlotClick(slot)
//            }
//        }
//    }
//
//    override fun getItemCount() = slots.size
//
//    fun setSelectedSlot(slot: SlotData?) {
//        selectedSlot = slot
//        notifyDataSetChanged()
//    }
//
//    fun setMyBookedSlot(slot: SlotData?) {
//        myBookedSlot = slot
//        notifyDataSetChanged()
//    }
//
//    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
//        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
//        val from1 = format.parse(slot1.slot_from)
//        val to1 = format.parse(slot1.slot_to)
//        val from2 = format.parse(slot2.slot_from)
//        val to2 = format.parse(slot2.slot_to)
//
//        return from1 < to2 && from2 < to1
//    }
//}
