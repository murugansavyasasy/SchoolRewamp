package com.vs.schoolmessenger.Parent.LSRW.MySubmissionModel

data class ActivityResponse (
    val status: Boolean,
    val message: String,
    val data: List<ActivityData>
)