package com.vs.schoolmessenger.School.AbsenteesMarking

import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentListData

interface AbsenteesSelectionListener {
    fun onSelectionChanged(selectedIds: List<GetAttendanceStudentListData>)
}