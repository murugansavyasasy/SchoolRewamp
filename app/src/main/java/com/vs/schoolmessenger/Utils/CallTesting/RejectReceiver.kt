package com.vs.schoolmessenger.Utils.CallTesting

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
class RejectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("RejectReceiver", "Call rejected")
// Stop the service to remove notification
        context.stopService(Intent(context, IncomingCallService::class.java))
    }
}