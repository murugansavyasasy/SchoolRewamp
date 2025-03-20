package com.vs.schoolmessenger.Auth.Country

data class Country( val id: Int,
                    val name: String,
                    val code: Int,
                    val mobile_number_length: Int,
                    val mobile_no_hint: String,
                    val base_url: String,
                    val reporting_url: String,
                    val flag_url: String)
