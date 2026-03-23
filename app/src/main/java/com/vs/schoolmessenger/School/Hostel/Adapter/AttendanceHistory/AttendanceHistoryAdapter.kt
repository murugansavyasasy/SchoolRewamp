package com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory


import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getAttendanceHistoryData
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getRoomData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceHistoryAdapter(
    private var itemList: List<getRoomData>?,
    private var context: Context,
    private var isLoading: Boolean

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_history_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attendance_history_item, parent, false)
            DataViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DataViewHolder) {
            itemList?.get(position)?.let { holder.bind(it, position) }
        } else if (holder is ShimmerViewHolder) {
            holder.startShimmer()
        }
    }


    override fun getItemCount(): Int {
        return if (isLoading) 20 else itemList?.size ?: 0
    }

    fun updateData(newList: List<getRoomData>) {
        itemList = newList
        notifyDataSetChanged()
    }



    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblAttendanceDate: TextView = itemView.findViewById(R.id.lblAttendanceDate)
        private val consTotalStudentDetails: ConstraintLayout = itemView.findViewById(R.id.consTotalStudentDetails)
        private val consPresentDetails: ConstraintLayout = itemView.findViewById(R.id.consPresentDetails)
        private val consAbsentDetails: ConstraintLayout = itemView.findViewById(R.id.consAbsentDetails)


        fun bind(data: getRoomData, position: Int) {
//            lblAttendanceDate.text = data.date
            setDrawableBackgroundColor(consTotalStudentDetails,R.color.light_blue_8)
            setDrawableBackgroundColor(consPresentDetails,R.color.light_green_8)
            setDrawableBackgroundColor(consAbsentDetails,R.color.light_red_8)


        }
        fun setDrawableBackgroundColor(view: View, colorRes: Int) {
            val bgDrawable = view.background as? GradientDrawable
            bgDrawable?.setColor(ContextCompat.getColor(view.context, colorRes))
        }
    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}