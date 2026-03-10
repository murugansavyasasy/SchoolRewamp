package com.vs.schoolmessenger.Repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class RestClient {

    companion object {
        private var BASE_URL = "https://apiv8.schoolchimes.net/"
        private const val COUPON_URL = "https://api.pauket.com/api/partner/"

        private var retrofit: Retrofit? = null
        private var couponRetrofit: Retrofit? = null
        private var okHttpClient: OkHttpClient? = null

        private var _apiInterfaces: ApiInterfaces? = null
        private var _couponApiInterfaces: ApiInterfaces? = null

        val apiInterfaces: ApiInterfaces
            get() {
                if (_apiInterfaces == null) {
                    initDefaultRetrofit()
                }
                return _apiInterfaces!!
            }

        val couponApiInterfaces: ApiInterfaces
            get() {
                if (_couponApiInterfaces == null) {
                    initCouponRetrofit()
                }
                return _couponApiInterfaces!!
            }

        private fun initDefaultRetrofit() {
            if (okHttpClient == null) {
                createHttpClient()
            }

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            _apiInterfaces = retrofit!!.create(ApiInterfaces::class.java)
        }

        private fun initCouponRetrofit() {
            if (okHttpClient == null) {
                createHttpClient()
            }

            couponRetrofit = Retrofit.Builder()
                .baseUrl(COUPON_URL)
                .client(okHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            _couponApiInterfaces = couponRetrofit!!.create(ApiInterfaces::class.java)
        }

        private fun createHttpClient() {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            okHttpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build()
        }

        fun changeApiBaseUrl(newBaseUrl: String) {
            BASE_URL = newBaseUrl
            retrofit = null
            _apiInterfaces = null
            initDefaultRetrofit()
        }



        val client: Retrofit
            get() {
                if (retrofit == null) {
                    initDefaultRetrofit()
                }
                return retrofit!!
            }
    }
}

