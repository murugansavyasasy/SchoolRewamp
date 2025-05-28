package com.vs.schoolmessenger.School.AbsenteesReport.Listener

import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student

interface AbsenteesStudentClickListener {
    fun onHeaderItemClicked(position: Int, student: Student)
}