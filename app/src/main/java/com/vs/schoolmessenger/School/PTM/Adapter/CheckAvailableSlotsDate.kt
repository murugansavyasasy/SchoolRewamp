package com.vs.schoolmessenger.School.PTM.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.PTM.DataClass.AvailableSlotGroup
import com.vs.schoolmessenger.School.PTM.DataClass.SlotAvailability
import com.vs.schoolmessenger.School.PTM.DataClass.ValidatedSlot

class CheckAvailableSlotsDate(
    private val context: Context,
    private val dates: List<AvailableSlotGroup>,
    private val onUpdate: (List<Pair<String, SlotAvailability>>) -> Unit
) : RecyclerView.Adapter<CheckAvailableSlotsDate.ViewHolder>() {

    // Store both date + slots per position
    private val allDaySlots = mutableMapOf<Int, Pair<String, List<SlotAvailability>>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val rcySlotTiming: RecyclerView = itemView.findViewById(R.id.rcySlotTiming)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.check_slots_available_date, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dayGroup = dates[position]

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val formattedDate = try {
            val parsedDate = inputFormat.parse(dayGroup.date)
            outputFormat.format(parsedDate!!)
        } catch (e: Exception) {
            dayGroup.date
        }

        holder.tvDate.text = formattedDate

        val adapter = SlotTimingLoadAdapter(dayGroup.slots, context) { updatedDaySlots ->
            allDaySlots[position] = dayGroup.date to updatedDaySlots

            val combined = allDaySlots.values.flatMap { (date, slots) ->
                slots.map { slot -> date to slot }
            }
            onUpdate(combined)
        }

        holder.rcySlotTiming.layoutManager = GridLayoutManager(context, 2)
        holder.rcySlotTiming.adapter = adapter

        if (!allDaySlots.containsKey(position)) {
            allDaySlots[position] = dayGroup.date to dayGroup.slots

            val combined = allDaySlots.values.flatMap { (date, slots) ->
                slots.map { slot -> date to slot }
            }
            onUpdate(combined)
        }
    }



    override fun getItemCount(): Int = dates.size
}


