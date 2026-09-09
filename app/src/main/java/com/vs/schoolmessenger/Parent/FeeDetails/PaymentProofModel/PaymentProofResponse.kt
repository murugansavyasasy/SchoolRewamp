package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize


data class PaymentProofResponse (
    val status: Boolean?,
    val message: String?,
    val data: List<PaymentProofData>?
): Parcelable
