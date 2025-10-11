package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.AvailableSlotGroup
import com.vs.schoolmessenger.School.PTM.DataClass.SlotAvailability
import java.text.SimpleDateFormat
import java.util.Locale

class CheckAvailableSlotsDate(
    private val context: Context,
    private val dates: List<AvailableSlotGroup>,
    private val onUpdate: (List<Pair<String, SlotAvailability>>) -> Unit,
    private val onAllRemoved: () -> Unit
) : RecyclerView.Adapter<CheckAvailableSlotsDate.ViewHolder>() {

    private val allDaySlots = mutableMapOf<Int, Pair<String, List<SlotAvailability>>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate = itemView.findViewById<android.widget.TextView>(R.id.tvDate)
        val rcySlotTiming = itemView.findViewById<RecyclerView>(R.id.rcySlotTiming)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.check_slots_available_date, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dates.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayGroup = dates[position]
        val formattedDate = formatDateForDisplay(dayGroup.date)
        holder.tvDate.text = formattedDate

        val adapter = SlotTimingLoadAdapter(
            dayGroup.slots.toMutableList(),
            context,
            onDayUpdate = { updatedDaySlots ->
                allDaySlots[position] = dayGroup.date to updatedDaySlots

                val combined = allDaySlots.values.flatMap { (date, slots) ->
                    slots.map { slot -> date to slot }
                }
                onUpdate(combined)

                if (updatedDaySlots.isEmpty()) {
                    (dates as MutableList).removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, dates.size)
                    if (dates.isEmpty()) {
                        onAllRemoved()
                    }
                }
            }
        )

        holder.rcySlotTiming.layoutManager = GridLayoutManager(context, 2)
        holder.rcySlotTiming.adapter = adapter
    }



    private fun formatDateForDisplay(input: String?): String {
        if (input.isNullOrBlank()) return ""

        val s = input.trim()
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "dd MMM yyyy", "dd MMMM yyyy", "MM/dd/yyyy", "MM-dd-yyyy")
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(s)
                if (parsed != null) {
                    val out = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed)
                    // convert month to lowercase: "16 Sep 2025" -> "16 sep 2025"
                    val parts = out.split(" ")
                    if (parts.size == 3) {
                        return "${parts[0]} ${parts[1].lowercase(Locale.getDefault())} ${parts[2]}"
                    }
                    return out
                }
            } catch (_: Exception) {

            }
        }
        try {
            if (s.matches(Regex("^\\d{8}\$"))) {
                val parsed = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(s)
                if (parsed != null) {
                    val out = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsed)
                    val parts = out.split(" ")
                    if (parts.size == 3) {
                        return "${parts[0]} ${parts[1].lowercase(Locale.getDefault())} ${parts[2]}"
                    }
                    return out
                }
            }
        } catch (_: Exception) { }

        Log.w("CheckAvailableSlotsDate", "Unable to parse date: '$input' (showing raw)")
        return try {
            val quick = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(s)
            if (quick != null) {
                val out = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(quick)
                val parts = out.split(" ")
                if (parts.size == 3) {
                    "${parts[0]} ${parts[1].lowercase(Locale.getDefault())} ${parts[2]}"
                } else out
            } else s
        } catch (_: Exception) {
            s
        }
    }
}
