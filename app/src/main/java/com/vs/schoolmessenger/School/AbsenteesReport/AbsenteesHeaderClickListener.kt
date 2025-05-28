package com.vs.schoolmessenger.School.AbsenteesReport

import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudents.Student

interface AbsenteesHeaderClickListener {
    fun onHeaderItemClicked(position: Int, student: Student)
}
