package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails

data class GetAttendanceStudentListData(
    val id: String,
    val name: String,
    val admission_no: String,
    val roll_no: String,
    var att_type: String,
    var att_status: String,
    var is_edit: String,
)