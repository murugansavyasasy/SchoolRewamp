package com.vs.schoolmessenger.School.Hostel.Adapter.AttendanceHistory

import com.vs.schoolmessenger.School.Hostel.Adapter.OutpassRequest.OutpassRequestWise



import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vs.schoolmessenger.R
import com.vs.schoolmessenger.School.Hostel.Model.AttendanceHistory.getRoomData
import com.vs.schoolmessenger.School.Hostel.Model.OutPassRequest.OutpassRequestList.StatusWiseOutpassRequestData
import com.vs.schoolmessenger.Utils.ShimmerUtil

class AttendanceHistorySessionWiseAdapter(
    private var itemList: List<getRoomData>?,
    private val context: Context,
    private var isLoading: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SHIMMER = 0
    private val TYPE_DATA = 1

    var fullList: List<getRoomData> = itemList ?: emptyList()
    private var filteredList: List<getRoomData> = fullList

    override fun getItemViewType(position: Int): Int {
        return if (isLoading) TYPE_SHIMMER else TYPE_DATA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SHIMMER) {
            val shimmerView =
                ShimmerUtil.wrapWithShimmer(parent, R.layout.attendance_history_session_wise_item)
            ShimmerViewHolder(shimmerView)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.attendance_history_session_wise_item, parent, false)
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

    fun updateData(newList: List<getRoomData>) {
        fullList = newList
        filteredList = newList
        notifyDataSetChanged()
    }


    fun getCurrentList(): List<getRoomData> {
        return itemList!!
    }



    class DataViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val lblRoomNumber: TextView = itemView.findViewById(R.id.lblRoomNumber)
        private val lblNoOfSessions: TextView = itemView.findViewById(R.id.lblNoOfSessions)
        val rcSessionWiseAttendanceHistory: RecyclerView =
            itemView.findViewById(R.id.rcSessionWiseAttendanceHistory)

        fun bind(
            data: getRoomData,
        ) {

            lblRoomNumber.text= "${context.getString(R.string.room)} ${data.room_no}"
            lblNoOfSessions.text="${data.sessions.size} Session"
            lblNoOfSessions.visibility= View.GONE

            if (data.sessions.isEmpty()) {
                rcSessionWiseAttendanceHistory.visibility = View.GONE
            } else {
                rcSessionWiseAttendanceHistory.visibility = View.VISIBLE
                rcSessionWiseAttendanceHistory.layoutManager = LinearLayoutManager(context)
                rcSessionWiseAttendanceHistory.isNestedScrollingEnabled = false
                rcSessionWiseAttendanceHistory.adapter = AttendanceHistoryAdapter(
                    data.sessions,
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