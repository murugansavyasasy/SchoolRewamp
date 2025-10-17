package com.vs.schoolmessenger.School.AbsenteesMarking

import com.vs.schoolmessenger.CommonScreens.RecipientDataClasses.NameAndIds
import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentListData

interface AbsenteesSelectionListener {
    fun onSelectionChanged(selectedIds: List<GetAttendanceStudentListData>)
    fun onIdCheck(data: GetAttendanceStudentListData)
    fun onIdUnchecked(data: GetAttendanceStudentListData)
}