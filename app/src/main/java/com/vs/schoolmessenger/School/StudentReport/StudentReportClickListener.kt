package com.vs.schoolmessenger.School.StudentReport

import com.vs.schoolmessenger.School.NoticeBoard.NoticeData

interface StudentReportClickListener {
    fun onMailClick(data: StudentReportData)
    fun onPhoneClick(data: StudentReportData)
    fun onMessageClick(data: StudentReportData)

}