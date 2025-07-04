package com.vs.schoolmessenger.School.Assignment.DataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssignmentSendingData(
    val isTitle: String,
    val isDescription: String,
    val isAssignmentType: String,
    val isDate: String,
    val isTime: String,
) : Parcelable