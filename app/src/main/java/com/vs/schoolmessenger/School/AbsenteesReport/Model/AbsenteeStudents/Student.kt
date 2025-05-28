package com.vs.schoolmessenger.School.AbsenteesReport.Model.AbsenteeStudents

data class Student (
    val student_id: String,
    val student_name: String,
    val admission_no: String,
    val roll_no: String,
    val photo_path: String,
    val primary_mobile: String
)