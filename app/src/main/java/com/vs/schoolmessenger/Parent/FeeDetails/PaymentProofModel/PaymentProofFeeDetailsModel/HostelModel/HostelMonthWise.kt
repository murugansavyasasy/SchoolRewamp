package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize



@Parcelize
data class HostelMonthWise (
    val month_id: String? = null,
    val month_name: String? = null,
    val actual_amount: String? = null,
    val pending_amount: String? = null,
    val discount_amount: String? = null,
    val paid_amount: String? = null,
    val is_enabled: Boolean? = null,
): Parcelable