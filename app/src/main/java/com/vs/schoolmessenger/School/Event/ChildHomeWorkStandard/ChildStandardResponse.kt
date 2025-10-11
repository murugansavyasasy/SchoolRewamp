package com.vs.schoolmessenger.School.Event.ChildHomeWorkStandard

data class ChildStandardResponse (
    val status: Boolean,
    val message: String,
    val data: List<TargetData>
)