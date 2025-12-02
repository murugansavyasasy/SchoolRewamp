package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
import com.vs.schoolmessenger.R
import java.text.SimpleDateFormat
import java.util.Locale

class ParentSlotTimingAdapter(
    private val slots: List<SlotData>,
    private val context: Context,
    private val allSlots: List<SlotData>,
    private val allSelectedSlots: List<SlotData>,
    private val onSlotClick: (SlotData) -> Unit
) : RecyclerView.Adapter<ParentSlotTimingAdapter.SlotViewHolder>() {

    private var selectedSlot: SlotData? = null
    private var myBookedSlot: SlotData? = null
    private var globalMyBookings: List<SlotData> = emptyList()
    private var disableAllSlots: Boolean = false

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

        val isOverlappingWithSelected = allSelectedSlots.any {
            it != slot && isOverlapping(slot, it)
        }
        val isOverlappingWithGlobalMyBookings = globalMyBookings.any {
            it != slot && isOverlapping(slot, it)
        }

        when {
            disableAllSlots -> {
                if (slot.my_booking) {
                    holder.card.setBackgroundResource(R.drawable.circle_background_green)
                    holder.tvSlotStatus.text = context.getString(R.string.booked)
                    holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                    holder.card.isEnabled = false
                } else {
                    holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                    holder.tvSlotStatus.text = context.getString(R.string.Available)
                    holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
                    holder.card.isEnabled = false
                }
            }

            slot.is_booked && !slot.my_booking -> {
                holder.card.setBackgroundResource(R.drawable.bg_gray)
                holder.tvSlotStatus.text = context.getString(R.string.not_available)
                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
                holder.card.isEnabled = false
            }

            slot.my_booking -> {
                holder.card.setBackgroundResource(R.drawable.circle_background_green)
                holder.tvSlotStatus.text = context.getString(R.string.booked)
                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.card.isEnabled = false
            }

            isOverlappingWithSelected || isOverlappingWithGlobalMyBookings -> {
                holder.card.setBackgroundResource(R.drawable.gray_bg_radius)
                holder.tvSlotStatus.text = context.getString(R.string.time_conflict)
                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.system_orange))
                holder.card.isEnabled = false
            }

            selectedSlot == slot -> {
                holder.card.setBackgroundResource(R.drawable.bg_btn_blue)
                holder.tvSlotStatus.text = context.getString(R.string.selected)
                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.tvSlotTime.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
                holder.card.isEnabled = true
            }

            else -> {
                holder.card.setBackgroundResource(R.drawable.outline_gray)
                holder.tvSlotStatus.text = context.getString(R.string.Available)
                holder.tvSlotStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
                holder.tvSlotTime.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
                holder.card.isEnabled = true
            }
        }

        holder.itemView.setOnClickListener {
            if (!slot.is_booked && !slot.my_booking && holder.card.isEnabled && !disableAllSlots) {
                val previous = selectedSlot
                val wasSelected = selectedSlot == slot
                selectedSlot = if (wasSelected) null else slot
                onSlotClick(slot.apply { isSelected = !wasSelected })

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

    fun setGlobalMyBookings(slots: List<SlotData>) {
        globalMyBookings = slots
        notifyDataSetChanged()
    }

    fun setDisableAllSlots(value: Boolean) {
        disableAllSlots = value
        notifyDataSetChanged()
    }

    private fun isOverlapping(slot1: SlotData, slot2: SlotData): Boolean {
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val from1Str = slot1.slot_from.trim().uppercase(Locale.getDefault())
            val to1Str = slot1.slot_to.trim().uppercase(Locale.getDefault())
            val from2Str = slot2.slot_from.trim().uppercase(Locale.getDefault())
            val to2Str = slot2.slot_to.trim().uppercase(Locale.getDefault())

            val isSlot1AM = from1Str.contains("AM")
            val isSlot2AM = from2Str.contains("AM")
            val isSlot1PM = from1Str.contains("PM")
            val isSlot2PM = from2Str.contains("PM")

            if (!((isSlot1AM && isSlot2AM) || (isSlot1PM && isSlot2PM))) return false

            val from1 = format.parse(from1Str)
            val to1 = format.parse(to1Str)
            val from2 = format.parse(from2Str)
            val to2 = format.parse(to2Str)

            if (from1 == null || to1 == null || from2 == null || to2 == null) return false
            from1 < to2 && from2 < to1
        } catch (e: Exception) {
            false
        }
    }
}