package com.vs.schoolmessenger.Parent.Coupon.CouponModel.CouponSummary

import com.google.gson.annotations.SerializedName

class CampaignsWrapper {
    @SerializedName("current_page")
    val currentPage: Int = 0

    @SerializedName("data")
    val data: MutableList<Summary?>? = null
}