package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

data class CampaignList(
    val current_page: Int,
    val data: List<CampaignItem>,
    val first_page_url: String,
    val from: Int,
    val next_page_url: String?,
    val path: String,
    val per_page: String,
    val prev_page_url: String?,
    val to: Int
)
