package com.vs.schoolmessenger.CommonScreens.Ads

data class AdsResponse(
    val status: Boolean,
    val message: String,
    val data: List<AdItem>
)