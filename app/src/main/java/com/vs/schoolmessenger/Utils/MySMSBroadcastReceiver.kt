package com.vs.schoolmessenger.Utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class MySMSBroadcastReceiver : BroadcastReceiver() {
    var otpListener: ((String) -> Unit)? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val extras = intent.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as? Status
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    val otp = extractOTP(message)
                    otpListener?.invoke(otp)
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.e("OTP", "SMS Retriever timed out")
                }
            }
        }
    }
    private fun extractOTP(message: String): String {
        val otpRegex = Regex("\\d{4,6}") // Adjust if your OTP is 6 digits
        return otpRegex.find(message)?.value ?: ""
    }
}