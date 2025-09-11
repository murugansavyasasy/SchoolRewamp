package com.vs.schoolmessenger.Parent.Timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class TimeTableAdapter(
    private var itemList: List<TimeTableListData>?,
    private val listener: TimeTableListener,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.item_timetable)
            DataViewHolder.ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timetable, parent, false)
            DataViewHolder(view, context)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            holder.bind(itemList!![position], position, listener, this)
        }
    }

    override fun getItemCount(): Int {
        return if (isLoading) 10 else itemList?.size ?: 0
    }

    fun updateData(newData: List<TimeTableListData>) {
        itemList = newData
        isLoading = false
        notifyDataSetChanged()
    }

    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(
            data: TimeTableListData,
            position: Int,
            listener: TimeTableListener,
            adapter: TimeTableAdapter
        ) {
            val startTime: TextView = itemView.findViewById(R.id.start_time)
            val endTime: TextView = itemView.findViewById(R.id.end_time)
            val subjectValue: TextView = itemView.findViewById(R.id.subject_value)
            val facultyName: TextView = itemView.findViewById(R.id.name_value)
            val durationValue: TextView = itemView.findViewById(R.id.duration_value)

            startTime.text = data.start_time.replace(" ", "\n")
            endTime.text = data.end_time.replace(" ", "\n")

            val durationOnly = data.duration.trim()
            durationValue.text = "${context.getString(R.string.Duration)} – $durationOnly"

            when (data.hour_type) {
                Constant.one -> {
                    // Hour Type 1: Show subject_name and faculty_name
                    subjectValue.text =
                        if (data.subject_name.isNotEmpty()) data.subject_name else Constant.iffin
                    facultyName.text =
                        if (data.facalty_name.isNotEmpty()) data.facalty_name else Constant.iffin
                }

                Constant.two -> {
                    // Hour Type 2: Show name and staff_name
                    subjectValue.text = if (data.name.isNotEmpty()) data.name else Constant.iffin
                    facultyName.text = if (data.staff_name.isNotEmpty()) data.staff_name else Constant.iffin
                }

                else -> {
                    subjectValue.text = Constant.iffin
                    facultyName.text = Constant.iffin
                }
            }
        }

        class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun startShimmer() {
                ShimmerUtil.startShimmer(itemView)
            }
        }
    }
}
