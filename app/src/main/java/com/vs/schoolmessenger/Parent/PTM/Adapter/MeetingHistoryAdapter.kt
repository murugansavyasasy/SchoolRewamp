package com.vs.schoolmessenger.Parent.PTM.Adapter

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.Parent.PTM.DataClass.MeetingItem
import com.vs.schoolmessenger.Parent.PTM.Listener.OnCancelClickListener
import com.vs.schoolmessenger.R

class MeetingHistoryAdapter(
    private var items: MutableList<MeetingListItem>,
    private val listener: OnCancelClickListener,
    private val onEmptyList: (Boolean) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var fullList: MutableList<MeetingListItem> = ArrayList(items)

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
            tvDate.text = formatDate(meeting.date)
            tvTime.text = meeting.time
            tvStatus.text = meeting.status

            val modeDrawable = when (meeting.mode.lowercase()) {
                "in person" -> R.drawable.person_2_black_bg
                "phone call" -> R.drawable.phone_icon_black
                "virtual" -> R.drawable.network_black_bg
                else -> 0
            }

            if (modeDrawable != 0) {
                val drawable = ContextCompat.getDrawable(itemView.context, modeDrawable)
                drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                tvMode.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null)
                tvMode.compoundDrawablePadding = 8
            } else {
                tvMode.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            }


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
                    itemView.findViewById<View>(R.id.joinButton).visibility = View.GONE
                }

                meeting.mode.equals("In Person", true) -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.GONE
                    itemView.findViewById<View>(R.id.joinButton).visibility = View.GONE
                }

                meeting.mode.equals("Phone Call", true) -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.VISIBLE
                    itemView.findViewById<View>(R.id.joinButton).visibility = View.GONE
                }

                meeting.mode.equals("Virtual", true) -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.GONE
                    itemView.findViewById<View>(R.id.joinButton).visibility = View.VISIBLE
                }

                else -> {
                    cancelButton.visibility = View.VISIBLE
                    callButton.visibility = View.VISIBLE
                    itemView.findViewById<View>(R.id.joinButton).visibility = View.GONE
                }
            }

            cancelButton.setOnClickListener {
                val context = itemView.context
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_cancel_meeting, null)
                val etReason = dialogView.findViewById<EditText>(R.id.etReason)
                val btnCancelMeeting = dialogView.findViewById<Button>(R.id.btnCancelMeeting)
                val ivClose = dialogView.findViewById<ImageView>(R.id.ivClose)
                val alertDialog = AlertDialog.Builder(context).setView(dialogView).create()
                ivClose.setOnClickListener { alertDialog.dismiss() }
                btnCancelMeeting.setOnClickListener {
                    val reason = etReason.text.toString().trim()
                    if (reason.isEmpty()) {
                        Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                    } else {
                        alertDialog.dismiss()
                        listener.onCancelClick(meeting, adapterPosition, reason)
                    }
                }
                alertDialog.show()
            }

            callButton.setOnClickListener {
                val context = itemView.context
                val phoneNumber = meeting.staff_mobile_no?.trim()?.takeIf { it.isNotEmpty() } ?: ""
                if (phoneNumber.isNotEmpty()) {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                }
            }

            val joinButton: Button = itemView.findViewById(R.id.joinButton)
            joinButton.setOnClickListener {
                val context = itemView.context
                val url = meeting.meeting_url?.trim()
                if (!url.isNullOrEmpty()) {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse(url)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Unable to open meeting link", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Meeting URL not available", Toast.LENGTH_SHORT).show()
                }
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
        var headerHasItems = false

        items.forEach {
            when (it) {
                is MeetingListItem.Header -> {
                    if (headerHasItems && currentHeader != null) {
                        newList.add(currentHeader!!)
                    }
                    currentHeader = it
                    headerHasItems = false
                }
                is MeetingListItem.Item -> {
                    headerHasItems = true
                    newList.add(it)
                }
            }
        }

        if (headerHasItems && currentHeader != null) {
            newList.add(0, currentHeader!!)
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
                                    meeting.status.lowercase().contains(query)  ||
                                    meeting.date.lowercase().contains(query) ||
                                    meeting.time.lowercase().contains(query)
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
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }

}

sealed class MeetingListItem {
    data class Header(val title: String) : MeetingListItem()
    data class Item(val meeting: MeetingItem) : MeetingListItem()
}
