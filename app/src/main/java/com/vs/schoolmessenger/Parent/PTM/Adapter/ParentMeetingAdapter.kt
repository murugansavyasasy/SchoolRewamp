package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    // Track selected slot per meeting (multiple selection)
    private val selectedSlotsMap = mutableMapOf<String, SlotData>() // key = unique meeting key

    inner class ParentMeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
        val tvParentName: TextView = itemView.findViewById(R.id.tvParentName)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val btnMeetingType: TextView = itemView.findViewById(R.id.btnMeetingType)
        val rvSlots: RecyclerView = itemView.findViewById(R.id.rvSlots)
        val tvProfileIcon: TextView = itemView.findViewById(R.id.tvProfileIcon)
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
        holder.tvSubject.text = meeting.subject_name
        holder.btnMeetingType.text = meeting.slots.firstOrNull()?.event_mode ?: "Meeting"

        val firstLetter = meeting.staff_name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvProfileIcon.text = firstLetter

        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)

        // Flatten all slots from all meetings
        val allSlots = meetings.flatMap { it.slots }
        val allSelectedSlots = selectedSlotsMap.values.toList()
        // Create a unique key for this meeting
        val meetingKey = "${meeting.staff_id}_${meeting.start_time}_${meeting.event_name}"
        val meetingSelectedSlot = selectedSlotsMap[meetingKey]
        val slotAdapter = ParentSlotTimingAdapter(
            slots = meeting.slots,
            allSlots = allSlots,
            allSelectedSlots = allSelectedSlots
        ) { slot ->
            selectedSlotsMap[meetingKey] = slot
            notifyDataSetChanged()
            onSlotSelected(meeting, slot)
        }

        holder.rvSlots.adapter = slotAdapter
        val myBookedSlot = meeting.slots.find { it.my_booking }
        slotAdapter.setSelectedSlot(meetingSelectedSlot)
        slotAdapter.setMyBookedSlot(myBookedSlot)
    }

    override fun getItemCount(): Int = meetings.size
}
