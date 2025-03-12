package com.vs.schoolmessenger.School.DailyCollection

import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudentHeaderData
import com.vs.schoolmessenger.School.AbsenteesReport.AbsenteesStudentHeaderListAdapter

interface Dcf1ClickListener {
    fun onItemClick(data: DcfData1, holder: DcfAdapter1.DataViewHolder)
}