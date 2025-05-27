package com.vs.schoolmessenger.School.AbsenteesReport

import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise

interface AbsenteesDetailClickListener {
    fun onItemClick(data: AbsenteesDetailData, holder: AbsenteesReportDetailAdapter.DataViewHolder)
    fun onClassSelected(data: ClassWise)

}