package com.vs.schoolmessenger.School.Hostel.Adapter.RoomAttendance




import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.RoomAttendance.RoomStudentAttendanceData
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.ShimmerUtil

class RoomAttendanceAdapter(
    private var itemList: List<RoomStudentAttendanceData>?,
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
            val shimmerView = ShimmerUtil.wrapWithShimmer(parent, R.layout.room_attendance_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.room_attendance_item, parent, false)
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

    fun updateData(newList: List<RoomStudentAttendanceData>) {
        itemList = newList
        notifyDataSetChanged()
    }



    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lblInitialName: TextView = itemView.findViewById(R.id.lblInitialName)
        private val lblStudentDetails: TextView = itemView.findViewById(R.id.lblStudentDetails)
        private val lblFullName: TextView = itemView.findViewById(R.id.lblFullName)
        private val lblPresent: MaterialButton = itemView.findViewById(R.id.lblPresent)
        private val lblAbsent: MaterialButton = itemView.findViewById(R.id.lblAbsent)


        fun bind(data: RoomStudentAttendanceData, position: Int) {
            lblInitialName.text = Constant.getInitials( data.name)
            lblStudentDetails.text="Student id :${data.studentId} Parent Mobile No :${data.parentPhone}"
            lblFullName.text=data.name

        }

    }

    class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun startShimmer() {
            ShimmerUtil.startShimmer(itemView)

        }
    }

}