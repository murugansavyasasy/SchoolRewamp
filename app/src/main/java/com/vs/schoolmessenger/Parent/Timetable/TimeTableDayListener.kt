package com.vs.schoolmessenger.Parent.Timetable

import com.vs.schoolmessenger.Parent.LSRW.LSRWAdapter
import com.vs.schoolmessenger.Parent.LSRW.LSRWData

interface TimeTableDayListener{

    fun onItemClick(data: TimeTableDayData, holder: TimeTableDayAdapter.DataViewHolder)
}
