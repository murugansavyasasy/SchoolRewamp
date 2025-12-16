package com.vs.schoolmessenger.Utils.CallTesting

import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Icon
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.vs.schoolmessenger.R

object PhoneAccountHelper {

    private const val ACCOUNT_ID = "school_voip_account"

    fun registerPhoneAccount(context: Context) {
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val component = ComponentName(context, MyConnectionService::class.java)

        val handle = PhoneAccountHandle(component, ACCOUNT_ID)

        val account = PhoneAccount.builder(handle, "SchoolMessenger VOIP")
            .setCapabilities(
                PhoneAccount.CAPABILITY_CALL_PROVIDER or
                        PhoneAccount.CAPABILITY_CONNECTION_MANAGER or
                        PhoneAccount.CAPABILITY_CALL_SUBJECT
            )
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setHighlightColor(Color.GREEN)
            .build()

        telecom.registerPhoneAccount(account)

        Log.d("PhoneAccountHelper", "PhoneAccount registered successfully")
    }

    fun getHandle(context: Context): PhoneAccountHandle {
        return PhoneAccountHandle(
            ComponentName(context, MyConnectionService::class.java),
            ACCOUNT_ID
        )
    }
}
