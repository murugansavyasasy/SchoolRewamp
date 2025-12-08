package com.vs.schoolmessenger.School.AbsenteesReport.Listener

interface OnAbsenteeClickListener {
    fun onAbsenteeClicked(
        absentOn: String,
        classId: String,
        sectionId: String,
        classname: String,
        sectionname: String,
        student_counts: String,
        absent: String,
        total: String
    )
}
