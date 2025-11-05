package com.vs.schoolmessenger.Dashboard.NewFeatures.Model

import com.vs.schoolmessenger.School.AbsenteesMarking.AbsenteesMarkingModel.GetAttendanceDetails.GetAttendanceStudentListData

class GetFeature (
    val status: Boolean,
    val message: String,
    val data: List<GetFeatureData>
)