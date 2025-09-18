package com.vs.schoolmessenger.School.AbsenteesReport.Listener

import com.vs.schoolmessenger.School.AbsenteesReport.Adapter.AbsenteesReportDetailAdapter
import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteesDetailData
import com.vs.schoolmessenger.School.AbsenteesReport.Model.ClassWise

interface AbsenteesDetailClickListener {
    fun onItemClick(data: AbsenteesDetailData)
    fun onClassSelected(data: ClassWise)

}