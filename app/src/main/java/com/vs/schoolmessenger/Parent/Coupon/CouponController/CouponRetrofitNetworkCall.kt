package com.vs.schoolmessenger.Parent.Coupon.CouponController

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CouponRetrofitNetworkCall {
    private const val BASE_URL = "https://api.pauket.com/api/partner/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: CouponAPIServiceInterface by lazy {
        retrofit.create(CouponAPIServiceInterface::class.java)
    }
}
