package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem
import com.vs.schoolmessenger.Parent.PTM.Listener.OnCancelClickListener
import com.vs.schoolmessenger.R

class MeetingHistoryAdapter(
    private val items: MutableList<MeetingListItem>, // mutable so we can remove items
    private val listener: OnCancelClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is MeetingListItem.Header -> TYPE_HEADER
            is MeetingListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.textview_item, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_meeting_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as MeetingListItem.Header)
            is ItemViewHolder -> holder.bind(items[position] as MeetingListItem.Item)
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.lblStatus)
        fun bind(header: MeetingListItem.Header) {
            tvHeader.text = header.title
        }
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvPurpose: TextView = view.findViewById(R.id.tvPurpose)
        private val tvStaff: TextView = view.findViewById(R.id.tvStaff)
        private val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        private val tvMode: TextView = view.findViewById(R.id.tvMode)
        private val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        private val cancelButton: TextView = view.findViewById(R.id.cancelButton)

        fun bind(item: MeetingListItem.Item) {
            val meeting = item.meeting
            tvPurpose.text = meeting.purpose
            tvStaff.text = "with ${meeting.staff_name}"
            tvSubject.text = meeting.subject_name
            tvMode.text = meeting.mode
            tvDuration.text = "15 min"
            tvDate.text = meeting.date
            tvTime.text = meeting.time
            tvStatus.text = meeting.status

            tvStatus.setBackgroundColor(
                if (meeting.status.equals("Completed", true)) Color.parseColor("#4CAF50")
                else Color.parseColor("#FFA500")
            )

            cancelButton.visibility =
                if (meeting.status.equals("Completed", true)) View.GONE else View.VISIBLE

            cancelButton.setOnClickListener {
                listener.onCancelClick(meeting, bindingAdapterPosition)
            }
        }
    }
    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
            cleanUpEmptyHeaders()
        }
    }
    private fun cleanUpEmptyHeaders() {
        val iterator = items.iterator()
        var lastHeaderIndex = -1
        var hasItemUnderHeader = false

        var index = 0
        while (iterator.hasNext()) {
            when (iterator.next()) {
                is MeetingListItem.Header -> {
                    if (lastHeaderIndex != -1 && !hasItemUnderHeader) {
                        items.removeAt(lastHeaderIndex)
                        notifyItemRemoved(lastHeaderIndex)
                        index--
                    }
                    lastHeaderIndex = index
                    hasItemUnderHeader = false
                }

                is MeetingListItem.Item -> {
                    hasItemUnderHeader = true
                }
            }
            index++
        }
        if (lastHeaderIndex != -1 && !hasItemUnderHeader) {
            items.removeAt(lastHeaderIndex)
            notifyItemRemoved(lastHeaderIndex)
        }
    }
}

sealed class MeetingListItem {
    data class Header(val title: String) : MeetingListItem()
    data class Item(val meeting: MeetingItem) : MeetingListItem()
}
