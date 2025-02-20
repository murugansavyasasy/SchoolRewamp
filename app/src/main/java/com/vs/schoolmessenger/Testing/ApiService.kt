package com.vs.schoolmessenger.Testing

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface ApiService {
    @Headers(
        "Partner-Name: voicesnaps",
        "api-key: b9634e2c3aa9b6fdc392527645c43871"
    )
    @GET("get_category_list")
    fun getCategories(): Call<CategoryResponse>

    @GET("get_campaigns")
    fun getCoupons(
        @HeaderMap headers: Map<String, String>,
        @QueryMap params: HashMap<String, String>
    ): Call<CouponResponse>
}

