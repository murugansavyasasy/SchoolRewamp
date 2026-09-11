package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.HostelModel

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.Month_details
import kotlinx.parcelize.Parcelize

@Parcelize
data class HostelFee (
    val hostel_name: String? = null,
    val room_no: String? = null,
    val bed_no: String? = null,
    val actual_amount: String? = null,
    val pending_amount: String? = null,
    val paid_amount: String? = null,
    val discount_amount: String? = null,
//    val month_wise_fee: List<HostelMonthWise>? = null,
    val month_details: List<HostelMonthWise>? = null

): Parcelable