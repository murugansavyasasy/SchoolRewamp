package com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudents

import com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeData

data class AbsenteeStudentsResponse (
    val status: Boolean,
    val message: String,
    val data: List<Student>
)