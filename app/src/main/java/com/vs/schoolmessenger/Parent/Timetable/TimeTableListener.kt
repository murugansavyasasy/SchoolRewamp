package com.vs.schoolmessenger.Parent.Timetable

interface TimeTableListener {

    fun onItemClick(data: TimeTableData, holder: TimeTableAdapter.DataViewHolder)
}
