package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem
import com.vs.schoolmessenger.Parent.PTM.Listener.OnCancelClickListener
import com.vs.schoolmessenger.R

class MeetingHistoryAdapter(
    private var items: MutableList<MeetingListItem>, // current displayed list
    private val listener: OnCancelClickListener,
    private val onEmptyList: (Boolean) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var fullList: MutableList<MeetingListItem> = ArrayList(items) // backup copy

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
        private val callButton: TextView = view.findViewById(R.id.callButton)


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


//            tvStatus.setBackgroundColor(
//                if (meeting.status.equals("Completed", true)) Color.parseColor("#5cc885")
//                else Color.parseColor("#4085ef")
//            )

            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(
                    if (meeting.status.equals("Completed", true))
                        Color.parseColor("#5cc885")
                    else
                        Color.parseColor("#4085ef")
                )
            }

            tvStatus.background = bgDrawable


            when {
                meeting.status.equals("Completed", true) -> {
                    cancelButton.visibility = View.GONE
                    callButton.visibility = View.GONE
                }
                meeting.mode.equals("In Person", true) -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.GONE
                }
                else -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.VISIBLE
                }
            }

            cancelButton.setOnClickListener {
                val context = itemView.context
                val dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_cancel_meeting, null)
                val etReason = dialogView.findViewById<EditText>(R.id.etReason)
                val btnCancelMeeting = dialogView.findViewById<Button>(R.id.btnCancelMeeting)
                val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)

                val alertDialog = AlertDialog.Builder(context)
                    .setView(dialogView)
                    .create()

                btnCancelMeeting.setOnClickListener {
                    alertDialog.dismiss()
                }
                ivClose.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }

            callButton.setOnClickListener {
                val context = itemView.context
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:${meeting.staff_phone}")
                }
                context.startActivity(intent)
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
        val newList = mutableListOf<MeetingListItem>()
        var currentHeader: MeetingListItem.Header? = null

        items.forEach {
            when (it) {
                is MeetingListItem.Header -> currentHeader = it
                is MeetingListItem.Item -> {
                    currentHeader?.let { newList.add(it) }
                    newList.add(it)
                    currentHeader = null
                }
            }
        }

        items = newList
        notifyDataSetChanged()
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val filteredList: MutableList<MeetingListItem> = mutableListOf()

                if (query.isEmpty()) {
                    filteredList.addAll(fullList)
                } else {
                    var currentHeader: MeetingListItem.Header? = null
                    val tempList: MutableList<MeetingListItem> = mutableListOf()

                    fullList.forEach { listItem ->
                        when (listItem) {
                            is MeetingListItem.Header -> {
                                currentHeader = listItem
                            }
                            is MeetingListItem.Item -> {
                                val meeting = listItem.meeting
                                if (
                                    meeting.purpose.lowercase().contains(query) ||
                                    meeting.staff_name.lowercase().contains(query) ||
                                    meeting.subject_name.lowercase().contains(query) ||
                                    meeting.status.lowercase().contains(query)
                                ) {
                                    currentHeader?.let {
                                        if (!tempList.contains(it)) tempList.add(it)
                                    }
                                    tempList.add(listItem)
                                }
                            }
                        }
                    }

                    filteredList.addAll(tempList)
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }


            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                items = (results?.values as? MutableList<MeetingListItem>) ?: mutableListOf()
                notifyDataSetChanged()

                val hasItem = items.any { it is MeetingListItem.Item }
                onEmptyList(!hasItem)
            }

        }
    }
}

sealed class MeetingListItem {
    data class Header(val title: String) : MeetingListItem()
    data class Item(val meeting: MeetingItem) : MeetingListItem()
}
