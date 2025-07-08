package com.vs.schoolmessenger.Parent.Timetable

interface TimeTableListener {

    fun onItemClick(data: TimeTableListData, holder: TimeTableAdapter.DataViewHolder)
}
