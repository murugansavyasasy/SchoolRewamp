package com.vs.schoolmessenger.School.AbsenteesReport.Listener

import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData

interface AbsenteesClickListener {
    fun onDateSelected(data: AbsenteeData)
}