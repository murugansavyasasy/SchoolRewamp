package com.vs.schoolmessenger.FCM

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Repository.StatusMessageModel
import com.vs.schoolmessenger.Utils.SharedPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeclineReceiver : BroadcastReceiver() {


    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        RingtoneHelper.stop()

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val id =
            intent.getIntExtra(
                "NOTIFICATION_ID",
                1001
            )

        manager.cancel(id)

        context.stopService(
            Intent(
                context,
                CallForegroundService::class.java
            )
        )
    }
}

//    override fun onReceive(context: Context, intent: Intent) {
//        val pendingResult = goAsync()
//        val data = intent.getSerializableExtra("DATA") as? HashMap<String, String>
//        val id = intent.getIntExtra("NOTIFICATION_ID", 1001)
//
//        RingtoneHelper.stop()
//        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        manager.cancel(id)
//        CallStateManager.markHandled(context, id)
//
//
//
//       CoroutineScope(Dispatchers.IO).launch {
//            try {
//
//                val mobile = SharedPreference.getMobileNumber(context)
//
//                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                    .format(Date())
//
//                val jsonObject = JsonObject().apply {
//                    addProperty("url", data?.get("url"))
//                    addProperty("duration", 0)
//                    addProperty("ei1", data?.get("ei5"))
//                    addProperty("ei2", data?.get("url"))
//                    addProperty("ei3"   , data?.get("ei3"))
//                    addProperty("ei4", "Android")
//                    addProperty("ei5", data?.get("ei5"))
//                    addProperty("start_time", currentTime)
//                    addProperty("end_time", currentTime)
//                    addProperty("retry_count", data?.get("retry_count"))
//                    addProperty("phone", mobile)
//                    addProperty("receiver_id", data?.get("receiver_id"))
//                    addProperty("circular_id", data?.get("circular_id"))
//                    addProperty("diallist_id", data?.get("ei5"))
//                    addProperty("call_status", "NO")
//                }
//                Log.d("DECLINE_API", "Request: $jsonObject")
//
//
//                RestClient.apiInterfaces.updateNotificationCallLog(jsonObject)
//                    ?.enqueue(object : Callback<StatusMessageModel?> {
//
//                        override fun onResponse(
//                            call: Call<StatusMessageModel?>,
//                            response: Response<StatusMessageModel?>
//                        ) {
//
//                            Log.d("DECLINE_API", "Code: ${response.code()}")
//                            Log.d("DECLINE_API", "Body: ${response.body()}")
//
//                            pendingResult.finish()
//                        }
//
//                        override fun onFailure(call: Call<StatusMessageModel?>, t: Throwable) {
//
//                            Log.e("DECLINE_API", "Error: ${t.message}")
//
//                            pendingResult.finish()
//                        }
//                    })
//
//
//            } catch (e: Exception) {
//                Log.e("DECLINE_API", "Error: ${e.message}")
//            } finally {
//            }
//        }
//    }
//}