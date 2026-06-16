package com.vs.schoolmessenger.Auth.Country

data class CountryResponse(
    val status: Boolean,
    val message: String,
    val data: List<Country>
)
