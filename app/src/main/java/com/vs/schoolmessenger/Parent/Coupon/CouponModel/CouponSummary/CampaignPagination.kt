package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

data class CampaignPagination(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<CampaignItem>,
    @SerializedName("first_page_url") val firstPageUrl: String?,
    val from: Int?,
    @SerializedName("next_page_url") val nextPageUrl: String?,
    val path: String?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("prev_page_url") val prevPageUrl: String?,
    val to: Int?
)
