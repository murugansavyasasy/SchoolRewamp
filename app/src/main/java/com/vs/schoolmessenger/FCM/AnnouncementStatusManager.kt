package com.vs.schoolmessenger.FCM

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Repository.ErrorResponse
import com.vs.schoolmessenger.Repository.RestClient
import com.vs.schoolmessenger.Repository.StatusMessageModel
import com.vs.schoolmessenger.Utils.SharedPreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AnnouncementStatusManager {
    fun sendStatus(
        context: Context,
        voiceUrl: String?,
        welcomeUrl: String?,
        notificationId: Int,
        welcome_file: String?,
        school_name: String?,
        member_name: String?,
        call_title: String?,
        ei1: String?,
        ei2: String?,
        ei3: String?,
        ei4: String?,
        ei5: String?,
        circularId: String?,
        receiverId: String?,
        retrycount: String?
    ) {
        val MobileNumber: String? = SharedPreference.getMobileNumber(context)

       val isStartTime = getNow()
       val isEndTime = isStartTime

        val jsonObject = JsonObject()
        jsonObject.addProperty("url", voiceUrl)
        jsonObject.addProperty("duration", 0)
        jsonObject.addProperty("ei1", ei1)
        jsonObject.addProperty("ei2", ei2)
        jsonObject.addProperty("ei3", ei3)
        jsonObject.addProperty("ei4", ei4)
        jsonObject.addProperty("ei5", ei5)
        jsonObject.addProperty("start_time", isStartTime)
        jsonObject.addProperty("end_time", isEndTime)
        jsonObject.addProperty("retry_count", retrycount)
        jsonObject.addProperty("phone", MobileNumber)
        jsonObject.addProperty("receiver_id", receiverId)
        jsonObject.addProperty("circular_id", circularId)
        jsonObject.addProperty("diallist_id", ei5)
        jsonObject.addProperty("call_status", "NO")
        Log.d("jsonObjectReq", jsonObject.toString())

        RestClient.apiInterfaces.updateNotificationCallLog(jsonObject)
            ?.enqueue(object : retrofit2.Callback<StatusMessageModel?> {
                override fun onResponse(
                    call: retrofit2.Call<StatusMessageModel?>,
                    response: retrofit2.Response<StatusMessageModel?>
                ) {
                    Log.d(
                        "isUpdateCallLog",
                        response.code().toString() + " - " + response.toString()
                    )
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val status = response.body()!!.status
                        }
                    } else {
                        val errorBodyString = response.errorBody()?.string()
                        val gson = Gson()
                        val errorModel = gson.fromJson(errorBodyString, ErrorResponse::class.java)
                        Toast.makeText(context, errorModel.message, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<StatusMessageModel?>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }
    private fun getNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}