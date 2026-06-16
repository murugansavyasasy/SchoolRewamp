package com.vs.schoolmessenger.School.Hostel.Adapter.MenuTimetable.DayWiseMessMenu

import android.annotation.SuppressLint
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.MessTimeTable.MessDayWiseMenu.MealItem

import com.vs.schoolmessenger.Utils.ShimmerUtil

class MessDetailsAdapter(
    private var itemList: List<MealItem>,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1
    private var fullList: List<MealItem> = itemList ?: listOf()
    private var filteredList: List<MealItem> = fullList
    private var expandedPosition = RecyclerView.NO_POSITION


    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.menu_details_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_details_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 20 else filteredList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder && !isLoading) {
            holder.bind(filteredList[position], context,position)

        }
    }

    fun updateData(newList: List<MealItem>) {
        fullList = newList
        filteredList = newList
        isLoading = false
        notifyDataSetChanged()
    }


    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblColor: TextView = itemView.findViewById(R.id.lblColor)
        private val lblMenuType: TextView = itemView.findViewById(R.id.lblMenuType)
        private val lblFoodAvailable: TextView = itemView.findViewById(R.id.lblFoodAvailable)


        private val colorList = listOf(
            R.color.green,
            R.color.dark_blue_color,
            R.color.red,
            R.color.dark_voilet_2,
            R.color.orange,
            R.color.yellow,
            R.color.bpDarker_red,
            R.color.dark_brown,
            R.color.pink_color,
            R.color.dark_green_2,
            R.color.teacher_clr_grey_dark
        )

        @SuppressLint("SetTextI18n")
        fun bind(
            data: MealItem,
            context: Context,
            position: Int,
        ) {
            lblMenuType.text=data.mealName
            lblFoodAvailable.text=data.items

            val color = if (position < colorList.size) {
                colorList[position]   // first 10 ordered
            } else {
                colorList.random()   // after 10 random
            }

            lblColor.setBackgroundColor(ContextCompat.getColor(context, color))

        }

    }


    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            ShimmerUtil.startShimmer(itemView)
        }
    }
}