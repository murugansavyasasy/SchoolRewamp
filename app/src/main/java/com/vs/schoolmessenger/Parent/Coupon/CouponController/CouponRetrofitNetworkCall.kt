package com.vs.schoolmessenger.Parent.Coupon.CouponController

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CouponRetrofitNetworkCall {

    private const val BASE_URL = "https://api.pauket.com/api/partner/"
    private var defaultRetrofit: Retrofit? = null

    fun getClient(): Retrofit {
        return if (defaultRetrofit == null) {
            Log.d("Retrofit", "Creating Retrofit client with base URL: $BASE_URL")
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().also {
                    defaultRetrofit = it
                }

        } else {
            Log.d("Retrofit", "Returining existing client with base ur: $BASE_URL")
            defaultRetrofit!!
        }
    }

    fun getClientWithBaseUrl(baseUrl: String): Retrofit {
        Log.d("Retrofit", "Creating Retrofit cleint with custom base URL : $BASE_URL")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}