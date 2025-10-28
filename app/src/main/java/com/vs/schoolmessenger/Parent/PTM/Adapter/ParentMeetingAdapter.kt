package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingData
import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
import com.vs.schoolmessenger.R

class ParentMeetingAdapter(
    private val meetings: List<MeetingData>,
    private val onSlotSelected: (MeetingData, SlotData) -> Unit
) : RecyclerView.Adapter<ParentMeetingAdapter.ParentMeetingViewHolder>() {

    // Shared global lists for overlap checking
    private val allSlotsList = mutableListOf<SlotData>()
    private val selectedSlotsList = mutableListOf<SlotData>()

    // Stores which slot is selected per meeting
    private val selectedSlotsMap = mutableMapOf<String, SlotData>()

    init {
        // Combine all meeting slots for global overlap detection
        meetings.forEach { meeting ->
            allSlotsList.addAll(meeting.slots)
        }
    }

    inner class ParentMeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
        val tvParentName: TextView = itemView.findViewById(R.id.tvParentName)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val btnMeetingType: TextView = itemView.findViewById(R.id.btnMeetingType)
        val rvSlots: RecyclerView = itemView.findViewById(R.id.rvSlots)
        val tvProfileIcon: TextView = itemView.findViewById(R.id.tvProfileIcon)
        val imgMeetingType: ImageView = itemView.findViewById(R.id.imgMeetingType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentMeetingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_meeting_item, parent, false)
        return ParentMeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentMeetingViewHolder, position: Int) {
        val meeting = meetings[position]

        holder.tvMeetingTitle.text = meeting.event_name
        holder.tvParentName.text = meeting.staff_name

        val subjectNames = meeting.slots.mapNotNull { it.subject_name }.distinct().joinToString(", ")
        holder.tvSubject.text = subjectNames.ifEmpty { "No Subject" }

        val mode = meeting.slots.firstOrNull()?.event_mode ?: "Meeting"
        holder.btnMeetingType.text = mode

        val firstLetter = meeting.staff_name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvProfileIcon.text = firstLetter

        val modeIconRes = when (mode.lowercase()) {
            "in person" -> R.drawable.person_white_bg
            "phone call" -> R.drawable.phone_icon_bg
            "virtual" -> R.drawable.network
            else -> R.drawable.phone_icon_bg
        }
        holder.imgMeetingType.setImageResource(modeIconRes)

        // Setup Slots RecyclerView
        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)

        val meetingKey = "${meeting.staff_id}_${meeting.start_time}_${meeting.event_name}"
        val meetingSelectedSlot = selectedSlotsMap[meetingKey]
        val myBookedSlot = meeting.slots.find { it.my_booking }

        val slotAdapter = ParentSlotTimingAdapter(
            slots = meeting.slots,
            allSlots = allSlotsList,
            allSelectedSlots = selectedSlotsList
        ) { slot ->
            // Update the selected slot for this meeting
            if (selectedSlotsMap[meetingKey] == slot) {
                // Deselect if same slot clicked again
                selectedSlotsMap.remove(meetingKey)
                selectedSlotsList.remove(slot)
            } else {
                // Add or replace selection
                selectedSlotsMap[meetingKey]?.let { old ->
                    selectedSlotsList.remove(old)
                }
                selectedSlotsMap[meetingKey] = slot
                if (!selectedSlotsList.contains(slot))
                    selectedSlotsList.add(slot)
            }

            // Refresh all meetings to recheck overlaps
            notifyDataSetChanged()
            onSlotSelected(meeting, slot)
        }

        slotAdapter.setSelectedSlot(meetingSelectedSlot)
        slotAdapter.setMyBookedSlot(myBookedSlot)
        holder.rvSlots.adapter = slotAdapter
    }

    override fun getItemCount(): Int = meetings.size
}














//package com.vs.schoolmessenger.Parent.PTM.Adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingData
//import com.vs.schoolmessenger.Parent.PTM.DataClass.SlotData
//import com.vs.schoolmessenger.R
//
//class ParentMeetingAdapter(
//    private val meetings: List<MeetingData>,
//    private val onSlotSelected: (MeetingData, SlotData) -> Unit
//) : RecyclerView.Adapter<ParentMeetingAdapter.ParentMeetingViewHolder>() {
//
//    private val selectedSlotsMap = mutableMapOf<String, SlotData>()
//
//    inner class ParentMeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
//        val tvParentName: TextView = itemView.findViewById(R.id.tvParentName)
//        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
//        val btnMeetingType: TextView = itemView.findViewById(R.id.btnMeetingType)
//        val rvSlots: RecyclerView = itemView.findViewById(R.id.rvSlots)
//        val tvProfileIcon: TextView = itemView.findViewById(R.id.tvProfileIcon)
//        val imgMeetingType: ImageView = itemView.findViewById(R.id.imgMeetingType)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentMeetingViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.parent_meeting_item, parent, false)
//        return ParentMeetingViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ParentMeetingViewHolder, position: Int) {
//        val meeting = meetings[position]
//        holder.tvMeetingTitle.text = meeting.event_name
//        holder.tvParentName.text = meeting.staff_name
//        holder.tvSubject.text = meeting.slots[0].subject_name.joinToString(", ")
//        val mode = meeting.slots.firstOrNull()?.event_mode ?: "Meeting"
//        holder.btnMeetingType.text = mode
//        val firstLetter = meeting.staff_name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
//        holder.tvProfileIcon.text = firstLetter
//
//        val modeIconRes = when (mode.lowercase()) {
//            "in person" -> R.drawable.person_white_bg
//            "phone call" -> R.drawable.phone_icon_bg
//            "virtual" -> R.drawable.network
//            else -> R.drawable.phone_icon_bg
//        }
//        holder.imgMeetingType.setImageResource(modeIconRes)
//
//        // Setup slots RecyclerView
//        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)
//        val allSlots = meetings.flatMap { it.slots }
//        val allSelectedSlots = selectedSlotsMap.values.toList()
//        val meetingKey = "${meeting.staff_id}_${meeting.start_time}_${meeting.event_name}"
//        val meetingSelectedSlot = selectedSlotsMap[meetingKey]
//        val slotAdapter = ParentSlotTimingAdapter(
//            slots = meeting.slots,
//            allSlots = allSlots,
//            allSelectedSlots = allSelectedSlots
//        ) { slot ->
//            selectedSlotsMap[meetingKey] = slot
//            notifyDataSetChanged()
//            onSlotSelected(meeting, slot)
//        }
//        holder.rvSlots.adapter = slotAdapter
//        val myBookedSlot = meeting.slots.find { it.my_booking }
//        slotAdapter.setSelectedSlot(meetingSelectedSlot)
//        slotAdapter.setMyBookedSlot(myBookedSlot)
//    }
//    override fun getItemCount(): Int = meetings.size
//}
