package com.vs.schoolmessenger.School.StudentReport

interface StudentReportClickListener {
    fun onMailClick(data: StudentReportData)
    fun onPhoneClick(data: StudentReportData)
    fun onMessageClick(data: StudentReportData)
}