package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

data class CampaignData(
    @SerializedName("total_count") val totalCount: Int,
    val campaigns: CampaignPagination
)
