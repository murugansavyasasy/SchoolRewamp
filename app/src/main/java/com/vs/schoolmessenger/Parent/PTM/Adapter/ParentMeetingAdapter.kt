package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.content.Context
import android.util.Log
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
    private val context: Context,
    private val onSlotSelected: (MeetingData, SlotData?, Boolean) -> Unit
) : RecyclerView.Adapter<ParentMeetingAdapter.ParentMeetingViewHolder>() {

    private val allSlotsList = mutableListOf<SlotData>()
    private val selectedSlotsMap = mutableMapOf<String, SlotData?>()
    private val allMyBookedSlots = mutableListOf<SlotData>()

    init {
        meetings.forEach { meeting ->
            allSlotsList.addAll(meeting.slots)
            allMyBookedSlots.addAll(meeting.slots.filter { it.my_booking })
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

        val subjectNames = meeting.slots
            .flatMap { it.subject_name ?: emptyList() }
            .distinct()
            .joinToString(", ")
        holder.tvSubject.text = subjectNames.ifEmpty { context.getString(R.string.no_subject) }


        val mode = meeting.slots.firstOrNull()?.event_mode ?: context.getString(R.string.meeting)
        holder.btnMeetingType.text = mode
        Log.d("mode", mode)

        val firstLetter =
            meeting.staff_name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvProfileIcon.text = firstLetter

        val modeIconRes = when (mode.lowercase()) {
            "in person", "in-person", "person","In Person" -> R.drawable.person_white_bg
            "phone call", "call", "phone","Phone","Call" -> R.drawable.phone_icon_bg
            "virtual", "online", "video call", "zoom" -> R.drawable.network
            else -> R.drawable.phone_icon_bg
        }

        holder.imgMeetingType.setImageResource(modeIconRes)

        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)

        val meetingKey = "${meeting.staff_id}_${meeting.start_time}_${meeting.event_name}"
        val selectedSlot = selectedSlotsMap[meetingKey]
        val myBookedSlot = meeting.slots.find { it.my_booking }

        val slotAdapter = ParentSlotTimingAdapter(
            slots = meeting.slots,
            context,
            allSlots = allSlotsList,
            allSelectedSlots = selectedSlotsMap.values.filterNotNull()
        ) { clickedSlot ->

            val previouslySelectedSlot = selectedSlotsMap[meetingKey]
            val isSameSlot = previouslySelectedSlot == clickedSlot

            // Deselect logic
            if (isSameSlot) {
                selectedSlotsMap[meetingKey] = null
                onSlotSelected(meeting, clickedSlot, false)
            } else {
                // Remove previous slot from this meeting
                previouslySelectedSlot?.let {
                    onSlotSelected(meeting, it, false)
                }

                // Add the new one
                selectedSlotsMap[meetingKey] = clickedSlot
                onSlotSelected(meeting, clickedSlot, true)
            }

            notifyDataSetChanged()
        }

        slotAdapter.setSelectedSlot(selectedSlot)
        slotAdapter.setMyBookedSlot(myBookedSlot)
        slotAdapter.setGlobalMyBookings(allMyBookedSlots)

        if (meeting.slots.any { it.my_booking }) {
            slotAdapter.setDisableAllSlots(true)
        }

        holder.rvSlots.adapter = slotAdapter
    }

    override fun getItemCount(): Int = meetings.size
}