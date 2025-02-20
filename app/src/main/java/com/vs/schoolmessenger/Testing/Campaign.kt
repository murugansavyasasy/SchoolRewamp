package com.vs.schoolmessenger.Testing

data class Campaign(
    val source_link: String,
    val campaign_name: String,
    val campaign_type: String,
    val thumbnail: String,
    val expiry_date: String?,
    val end_date: String,
    val offer_type: String,
    val discount: Int,
    val merchant_name: String,
    val category_name: String,
    val category_image: String,
    val merchant_logo: String
)