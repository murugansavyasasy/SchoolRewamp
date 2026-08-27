package com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails

class GetAttendanceStudent(
    val is_edit: Boolean,
    val total_strength: String?=null,
    val attd_details: List<GetAttendanceStudentListData>
)