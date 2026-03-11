package com.vs.schoolmessenger.School.Hostel.Adapter.MenuTimetable.DayWiseMessMenu





import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessDayWiseMenu.DayMenuData

import com.vs.schoolmessenger.Utils.ShimmerUtil

class DayWiseMessMenu(
    private var itemList: List<DayMenuData>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    var fullList: List<DayMenuData> = itemList ?: emptyList()
    private var filteredList: List<DayMenuData> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.day_wise_mess_menu)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.day_wise_mess_menu, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(filteredList[position])
        }
    }

    fun updateData(newList: List<DayMenuData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }


    fun getCurrentList(): List<DayMenuData> {
        return itemList!!
    }


    fun removeItemById(id: String) {
        val updatedList = fullList.mapNotNull { monthData ->
            val updatedDetails = monthData.meals.filterNot { it.id == id }
            if (updatedDetails.isNotEmpty()) {
                DayMenuData(day = monthData.day, meals = updatedDetails)
            } else null
        }
        updateData(updatedList)
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblDay: TextView = itemView.findViewById(R.id.lblDay)
        val rcMenuDetails: RecyclerView =
            itemView.findViewById(R.id.rcMenuDetails)

        fun bind(
            data: DayMenuData
        ) {
            lblDay.text=data.day

            if (data.meals.isEmpty()) {
                rcMenuDetails.visibility = View.GONE
            } else {
                rcMenuDetails.visibility = View.VISIBLE
                rcMenuDetails.layoutManager = LinearLayoutManager(context)
                rcMenuDetails.isNestedScrollingEnabled = false
                rcMenuDetails.adapter = MessDetailsAdapter(
                    data.meals,
                    context,
                    false
                )
            }

        }


    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}