package com.vs.schoolmessenger.School.StudentReport.StudentFeeDetails.Model

import com.google.gson.annotations.SerializedName

data class FeeDetailsData(
    @SerializedName("summary")
    val summary: FeeSummary? = null,

    @SerializedName("term_fees")
    val termFees: List<TermFee>? = null,

    @SerializedName("other_fees")
    val otherFees: List<OtherFee>? = null,

    @SerializedName("transport_fee")
    val transportFee: List<TransportFee>? = null,

    @SerializedName("hostel_fee")
    val hostelFee: List<HostelFee>? = null,

    @SerializedName("carryover_fees")
    val carryoverFees: List<CarryoverFee>? = null,

    @SerializedName("quantity_fees")
    val quantityFees: List<QuantityFee>? = null
)