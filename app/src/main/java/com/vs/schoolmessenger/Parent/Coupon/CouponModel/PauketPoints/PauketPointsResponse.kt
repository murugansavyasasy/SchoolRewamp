package com.vs.schoolmessenger.Parent.Coupon.CouponModel.PauketPoints

data class PauketPointsResponse(
    val status: Boolean,
    val message: String,
    val data: List<PauketPointsData>
)