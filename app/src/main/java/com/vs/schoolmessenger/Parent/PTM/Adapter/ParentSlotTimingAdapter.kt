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

        // Check overlapping with booked or selected slots
        val isOverlappingWithBooked = allSlots.any {
            it != slot && (it.is_booked || it.my_booking) && isOverlapping(slot, it)
        }
        val isOverlappingWithSelected = allSelectedSlots.any {
            it != slot && isOverlapping(slot, it)
        }

        // Apply UI based on status
        when {
            slot.is_booked && !slot.my_booking -> {
                // Already booked by someone else
                holder.card.setBackgroundResource(R.drawable.redeemed_background)
                holder.tvSlotStatus.text = "Booked"
                holder.tvSlotStatus.setTextColor(Color.WHITE)
                holder.card.isEnabled = false
            }

            slot.my_booking -> {
                // Booked by me
                holder.card.setBackgroundResource(R.drawable.green_bg_radius)
                holder.tvSlotStatus.text = "My Booking"
                holder.tvSlotStatus.setTextColor(Color.WHITE)
                holder.card.isEnabled = false
            }

            isOverlappingWithBooked || isOverlappingWithSelected -> {
                // Overlapping slot
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.tvSlotStatus.text = "Overlapping"
                holder.tvSlotStatus.setTextColor(Color.DKGRAY)
                holder.card.isEnabled = false
            }

            selectedSlot == slot -> {
                // Currently selected by user
                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
                holder.tvSlotStatus.text = "Selected"
                holder.tvSlotStatus.setTextColor(Color.WHITE)
                holder.card.isEnabled = true
            }

            else -> {
                // Available slot
                holder.card.setBackgroundResource(R.drawable.white_radious)
                holder.tvSlotStatus.text = ""
                holder.card.isEnabled = true
            }
        }

        // Handle click
        holder.itemView.setOnClickListener {
            if (!slot.is_booked && !slot.my_booking && holder.card.isEnabled) {
                val previous = selectedSlot
                selectedSlot = if (selectedSlot == slot) null else slot
                onSlotClick(selectedSlot ?: slot)

                // Refresh UI
                previous?.let {
                    val oldIndex = slots.indexOf(it)
                    if (oldIndex != -1) notifyItemChanged(oldIndex)
                }
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = slots.size

    fun setSelectedSlot(slot: SlotData?) {
        selectedSlot = slot
        notifyDataSetChanged()
    }

    fun setMyBookedSlot(slot: SlotData?) {
        myBookedSlot = slot
        notifyDataSetChanged()
    }

    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val from1 = format.parse(slot1.slot_from.trim())
            val to1 = format.parse(slot1.slot_to.trim())
            val from2 = format.parse(slot2.slot_from.trim())
            val to2 = format.parse(slot2.slot_to.trim())

            if (from1 == null || to1 == null || from2 == null || to2 == null) return false
            from1 < to2 && from2 < to1
        } catch (e: Exception) {
            false
        }
    }
}












//package com.vs.schoolmessenger.Parent.PTM.Adapter
//
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
//import com.vs.schoolmessenger.R
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class ParentSlotTimingAdapter(
//    private val slots: List<SlotData>,
//    private val allSlots: List<SlotData>,
//    private val allSelectedSlots: List<SlotData>,
//    private val onSlotClick: (SlotData) -> Unit
//) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {
//
//    private var selectedSlot: SlotData? = null
//    private var myBookedSlot: SlotData? = null
//
//    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
//        val tvSlotStatus: TextView = itemView.findViewById(R.id.tvSlotStatus)
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
//        holder.tvSlotTime.text = "${slot.slot_from} - ${slot.slot_to}"
//
//        val isOverlappingWithAnyBooked = allSlots.any {
//            it != slot && (it.my_booking) && isOverlapping(slot, it)
//        }
//
//        val isOverlappingWithSelected = allSelectedSlots.any {
//            it != slot && isOverlapping(slot, it)
//        }
//
//        // Logic to determine slot background, text color, and enable/disable state
//        when {
//            slot.is_booked && !slot.my_booking -> {
//                // Slot booked by someone else
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Not Available"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = false
//            }
//
//            slot.my_booking -> {
//                // My booked slot
//                holder.card.setBackgroundResource(R.drawable.bg_btn_blue) // Blue background
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.tvSlotStatus.text = "Booked"
//                holder.card.isEnabled = false
//            }
//
//            myBookedSlot != null -> {
//                // Disable all other slots if one myBooking exists
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
//                holder.card.isEnabled = false
//            }
//
//            isOverlappingWithAnyBooked -> {
//                // Disable overlapping slots
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Not Available"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = false
//            }
//
//            isOverlappingWithSelected -> {
//                // Disable slots overlapping with selected slot
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Time conflict"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = false
//            }
//
//            selectedSlot == slot -> {
//                // Currently selected slot
//                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.card.isEnabled = true
//            }
//
//            selectedSlot != null && isOverlapping(slot, selectedSlot!!) -> {
//                // Disable slot if it overlaps with selected
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Time conflict"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = false
//            }
//
//            else -> {
//                // Default available slot
//                holder.card.setBackgroundResource(R.drawable.outline_gray)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.tvSlotStatus.text = ""
//                holder.card.isEnabled = true
//            }
//        }
//
//        // Click listener for slot selection
//        holder.itemView.setOnClickListener {
//            if (holder.card.isEnabled) {
//                selectedSlot = if (selectedSlot == slot) null else slot
//                selectedSlot?.let { onSlotClick(it) } // Callback
//                notifyDataSetChanged()
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
//    // Check if two slots overlap
//    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
//        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
//        val from1 = format.parse(slot1.slot_from.trim())
//        val to1 = format.parse(slot1.slot_to.trim())
//        val from2 = format.parse(slot2.slot_from.trim())
//        val to2 = format.parse(slot2.slot_to.trim())
//        if (from1 == null || to1 == null || from2 == null || to2 == null) return false
//        return from1 < to2 && from2 < to1
//    }
//}
























//package com.vs.schoolmessenger.Parent.PTM.Adapter
//
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
//import com.vs.schoolmessenger.R
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class ParentSlotTimingAdapter(
//    private val slots: List<SlotData>,
//    private val allSlots: List<SlotData>,
//    private val onSlotClick: (SlotData, Boolean) -> Unit // callback: selected/unselected
//) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {
//
//    private val selectedSlots = mutableListOf<SlotData>()
//
//    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
//        val tvSlotStatus: TextView = itemView.findViewById(R.id.tvSlotStatus)
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
//        holder.tvSlotTime.text = "${slot.slot_from} - ${slot.slot_to}"
//
//        // Check if slot overlaps with any selected slots
//        val isOverlappingWithSelected = selectedSlots.any { it != slot && isOverlapping(slot, it) }
//
//        // Set UI based on slot status
//        when {
//            slot.is_booked -> {
//                // Slot booked by others → green
//                holder.card.setBackgroundResource(R.drawable.circle_background_green)
//                holder.tvSlotStatus.text = "Booked"
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.card.isEnabled = false
//            }
//
//            slot.my_booking -> {
//                // My booking → blue
//                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
//                holder.tvSlotStatus.text = "Booked"
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.card.isEnabled = false
//            }
//
//            selectedSlots.contains(slot) -> {
//                // Currently selected → blue
//                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
//                holder.tvSlotStatus.text = "Selected"
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.card.isEnabled = true
//            }
//
//            isOverlappingWithSelected -> {
//                // Disabled due to overlapping
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Time conflict"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = false
//            }
//
//            else -> {
//                // Available slot → default
//                holder.card.setBackgroundResource(R.drawable.outline_gray)
//                holder.tvSlotStatus.text = ""
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.card.isEnabled = true
//            }
//        }
//
//        // Click listener for selection/unselection
//        holder.itemView.setOnClickListener {
//            if (!holder.card.isEnabled) return@setOnClickListener
//
//            if (selectedSlots.contains(slot)) {
//                // Unselect slot
//                selectedSlots.remove(slot)
//                onSlotClick(slot, false)
//            } else {
//                // Select slot
//                selectedSlots.add(slot)
//                onSlotClick(slot, true)
//            }
//            notifyDataSetChanged() // Update UI for overlaps
//        }
//    }
//
//    override fun getItemCount(): Int = slots.size
//
//    // Utility function to check overlapping time
//    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
//        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
//        val from1 = format.parse(slot1.slot_from.trim())
//        val to1 = format.parse(slot1.slot_to.trim())
//        val from2 = format.parse(slot2.slot_from.trim())
//        val to2 = format.parse(slot2.slot_to.trim())
//        if (from1 == null || to1 == null || from2 == null || to2 == null) return false
//        return from1 < to2 && from2 < to1
//    }
//
//    // Get all selected slots from adapter
//    fun getSelectedSlots(): List<SlotData> = selectedSlots
//}


//package com.vs.schoolmessenger.Parent.PTM.Adapter
//
//import android.graphics.Color
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
//import com.vs.schoolmessenger.R
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//class ParentSlotTimingAdapter(
//    private val slots: List<SlotData>,
//    private val allSlots: List<SlotData>,
//    private val allSelectedSlots: List<SlotData>,
//    private val onSlotClick: (SlotData) -> Unit
//) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {
//
//    private var selectedSlot: SlotData? = null
//    private var myBookedSlot: SlotData? = null
//
//    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvSlotTime: TextView = itemView.findViewById(R.id.tvSlotTime)
//        val tvSlotStatus: TextView = itemView.findViewById(R.id.tvSlotStatus)
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
//        val isOverlappingWithAnyBooked = allSlots.any {
//            it != slot && (it.is_booked || it.my_booking) && isOverlapping(slot, it)
//        }
//        val isOverlappingWithSelected = allSelectedSlots.any {
//            it != slot && isOverlapping(slot, it)
//        }
//
//        when {
//            slot.is_booked && !slot.my_booking -> {
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.tvSlotStatus.text = "Not Available"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//                holder.card.isEnabled = false
//            }
//
//            slot.my_booking -> {
//                holder.card.setBackgroundResource(R.drawable.circle_background_green)
//                holder.tvSlotTime.setTextColor(Color.BLACK)
//                holder.tvSlotStatus.setTextColor(Color.BLACK)
//                holder.tvSlotStatus.text = "Booked"
//                holder.card.isEnabled = false
//            }
//
//            myBookedSlot != null -> {
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.card.isEnabled = false
//                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
//            }
//
//            isOverlappingWithAnyBooked -> {
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.card.isEnabled = false
//                holder.tvSlotStatus.text = "Not Available"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//            }
//
//            isOverlappingWithSelected -> {
//                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
//                holder.card.isEnabled = false
//                holder.tvSlotStatus.text = "Time conflict"
//                holder.tvSlotStatus.setTextColor(Color.RED)
//            }
//
//            selectedSlot == slot -> {
//                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
//                holder.tvSlotTime.setTextColor(Color.WHITE)
//                holder.tvSlotStatus.setTextColor(Color.WHITE)
//                holder.card.isEnabled = true
//            }
//
//            selectedSlot != null && isOverlapping(slot, selectedSlot!!) -> {
//                holder.card.isEnabled = false
//            }
//
//            else -> {
//                holder.card.setBackgroundColor(Color.WHITE)
//                holder.card.setBackgroundResource(R.drawable.outline_gray)
//                holder.card.isEnabled = true
//            }
//        }
//
////        holder.itemView.setOnClickListener {
////            if (!slot.is_booked && !slot.my_booking && holder.card.isEnabled) {
////                onSlotClick(slot)
////            }
////        }
//
//        holder.itemView.setOnClickListener {
//            if (!slot.is_booked && !slot.my_booking && !isOverlappingWithAnyBooked && holder.card.isEnabled) {
//                selectedSlot = if (selectedSlot == slot) null else slot
//                selectedSlot?.let { onSlotClick(it) } // callback for selection
//                notifyDataSetChanged()
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
//
//    fun setMyBookedSlot(slot: SlotData?) {
//        myBookedSlot = slot
//        notifyDataSetChanged()
//    }
//
//    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
//        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
//        val from1 = format.parse(slot1.slot_from.trim())
//        val to1 = format.parse(slot1.slot_to.trim())
//        val from2 = format.parse(slot2.slot_from.trim())
//        val to2 = format.parse(slot2.slot_to.trim())
//        if (from1 == null || to1 == null || from2 == null || to2 == null) return false
//        return from1 < to2 && from2 < to1
//    }
//}