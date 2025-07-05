package com.vs.schoolmessenger.Parent.Coupon.CouponCredentials

import android.content.Context
import android.util.Log
import com.vs.schoolmessenger.Utils.SharedPreference

object AppCredentials {
    const val PARTNER_NAME = "savyasasy"
    const val API_KEY = "33adab6a67a9eee6e72be49acfb6c100"

    lateinit var isMobileNumber: String

    fun init(context: Context) {
        isMobileNumber = SharedPreference.getMobileNumber(context) ?: run {
            Log.w("AppCredentials", "Mobile number is null, using default empty string.")
            ""
        }
    }
}