package com.vs.schoolmessenger.School.AbsenteesReport.Listener

import com.vs.schoolmessenger.School.AbsenteesReport.Model.Student

interface AbsenteesStudentDetailClickListener {
    fun onFooterItemClicked(position: Int, student: Student)
    fun onSearchResultEmpty(isEmpty: Boolean)
}





