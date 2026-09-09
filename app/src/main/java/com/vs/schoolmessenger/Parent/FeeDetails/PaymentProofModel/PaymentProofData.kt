package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofDataModel.PaymentDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentProofData (
    val student_details: PaymentProofStudentDetails?,
    val payment_details: List<PaymentDetails>?
): Parcelable