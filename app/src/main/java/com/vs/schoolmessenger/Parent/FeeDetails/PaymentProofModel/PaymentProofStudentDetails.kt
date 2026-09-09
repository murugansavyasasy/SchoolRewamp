package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentProofStudentDetails (
    val student_id: String?,
    val student_name: String?,
    val class_name: String?,
    val section_name: String?
): Parcelable