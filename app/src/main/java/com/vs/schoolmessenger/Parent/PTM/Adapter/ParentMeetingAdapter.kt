//package com.vs.schoolmessenger.Parent.PTM.Adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
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
//    private val selectedSlotsMap = mutableMapOf<String, SlotData>() // event_name → selected slot
//
//    inner class ParentMeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
//        val tvParentName: TextView = itemView.findViewById(R.id.tvParentName)
//        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
//        val btnMeetingType: TextView = itemView.findViewById(R.id.btnMeetingType)
//        val rvSlots: RecyclerView = itemView.findViewById(R.id.rvSlots)
//        val tvProfileIcon: TextView = itemView.findViewById(R.id.tvProfileIcon)
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
//
//        holder.tvMeetingTitle.text = meeting.event_name
//        holder.tvParentName.text = meeting.staff_name
//        holder.tvSubject.text = meeting.subject_name
//        holder.btnMeetingType.text = meeting.slots.firstOrNull()?.event_mode ?: "Meeting"
//        holder.tvProfileIcon.text = meeting.staff_name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
//
//        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)
//
//        val slotAdapter = ParentSlotTimingAdapter(meeting.slots) { slot ->
//            selectedSlotsMap[meeting.event_name] = slot
//            notifyDataSetChanged()
//            onSlotSelected(meeting, slot)
//        }
//
//        // Pass all booked slots across all meetings for overlap check
//        val allBookedSlots = meetings.flatMap { it.slots }.filter { it.is_booked }
//        slotAdapter.setAllBookedSlots(allBookedSlots)
//
//        // Set my_booking slot
//        val myBookedSlot = meeting.slots.find { it.my_booking }
//        slotAdapter.setMyBookedSlot(myBookedSlot)
//
//        // Set already selected slot for this meeting
//        slotAdapter.setSelectedSlot(selectedSlotsMap[meeting.event_name])
//
//        holder.rvSlots.adapter = slotAdapter
//    }
//
//    override fun getItemCount(): Int = meetings.size
//}




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

    private var selectedSlot: SlotData? = null

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

        val slotAdapter = ParentSlotTimingAdapter(meeting.slots) { slot ->
            selectedSlot = slot
            notifyDataSetChanged()
            onSlotSelected(meeting, slot)
        }

        holder.rvSlots.adapter = slotAdapter

        // find if this meeting has my_booking
        val myBookedSlot = meeting.slots.find { it.my_booking }

        slotAdapter.setSelectedSlot(selectedSlot)
        slotAdapter.setMyBookedSlot(myBookedSlot)
    }

    override fun getItemCount(): Int = meetings.size
}
