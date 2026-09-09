package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.TransportModel

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel.PaymentProofFeeDetailsModel.OthersModel.Month_details
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransportFee (
    val route_id: String? = null,
    val route_name: String? = null,
    val stop_id: String? = null,
    val stop_name: String? = null,
    val route_fee_type_id: String? = null,
    val route_fee_type_name: String? = null,
    val actual_amount: String? = null,
    val paid_amount: String? = null,
    val pending_amount: String? = null,
    val discount_given: String? = null,
    val bus_month_details: List<BusMonthFeeDetails>? = null

): Parcelable