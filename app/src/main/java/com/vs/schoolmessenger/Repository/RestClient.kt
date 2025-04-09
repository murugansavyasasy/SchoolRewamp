package com.vs.schoolmessenger.Repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RestClient {

    companion object {
        private var BASE_URL = "http://apiv7.schoolchimes.net/"
        private var retrofit: Retrofit? = null
        private var okHttpClient: OkHttpClient? = null
        private var _apiInterfaces: ApiInterfaces? = null

        val apiInterfaces: ApiInterfaces
            get() {
                if (_apiInterfaces == null) {
                    initRetrofit()
                }
                return _apiInterfaces!!
            }

        private fun initRetrofit() {
            if (okHttpClient == null) {
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

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient!!)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            _apiInterfaces = retrofit!!.create(ApiInterfaces::class.java)
        }

        fun changeApiBaseUrl(newBaseUrl: String) {
            BASE_URL = newBaseUrl
            retrofit = null
            _apiInterfaces = null
            initRetrofit()
        }

        val client: Retrofit
            get() {
                if (retrofit == null) {
                    initRetrofit()
                }
                return retrofit!!
            }
    }
}
