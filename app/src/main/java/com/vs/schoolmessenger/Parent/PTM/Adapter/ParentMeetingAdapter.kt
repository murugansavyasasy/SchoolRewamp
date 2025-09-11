package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

    inner class ParentMeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMeetingTitle: TextView = itemView.findViewById(R.id.tvMeetingTitle)
        val tvParentName: TextView = itemView.findViewById(R.id.tvParentName)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val btnMeetingType: Button = itemView.findViewById(R.id.btnMeetingType)
        val rvSlots: RecyclerView = itemView.findViewById(R.id.rvSlots)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentMeetingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parent_meeting_item, parent, false)
        return ParentMeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentMeetingViewHolder, position: Int) {
        val meeting = meetings[position]

        // Meeting details
        holder.tvMeetingTitle.text = meeting.event_name
        holder.tvParentName.text = meeting.staff_name
        holder.tvSubject.text = meeting.subject_name
        holder.btnMeetingType.text = meeting.slots.firstOrNull()?.event_mode ?: "Meeting"

        // Child RecyclerView (Slots)
        holder.rvSlots.layoutManager = GridLayoutManager(holder.itemView.context, 2)
        val slotAdapter = ParentSlotTimingAdapter(meeting.slots) { slot ->
            onSlotSelected(meeting, slot)
        }
        holder.rvSlots.adapter = slotAdapter
    }

    override fun getItemCount(): Int = meetings.size
}
