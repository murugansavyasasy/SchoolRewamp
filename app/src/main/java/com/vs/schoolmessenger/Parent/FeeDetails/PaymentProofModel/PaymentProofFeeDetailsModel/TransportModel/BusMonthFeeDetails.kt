package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusMonthFeeDetails (
    val month_id: String? = null,
    val month_name: String? = null,
    val total_amount: String? = null,
    val fee_amount: String? = null,
    val discount_amount: String? = null,
    val paid_amount: String? = null,
    val pending_amount: String? = null,
    val is_enabled: Boolean? = true,
): Parcelable