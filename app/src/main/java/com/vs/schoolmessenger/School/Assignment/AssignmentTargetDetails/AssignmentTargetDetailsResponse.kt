package com.vs.schoolmessenger.School.Assignment.AssignmentTargetDetails

import com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard.TargetData

data class AssignmentTargetDetailsResponse(
    val status: Boolean,
    val message: String,
    val data: List<TargetData>
)