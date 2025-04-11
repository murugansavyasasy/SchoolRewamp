package com.vs.schoolmessenger.Repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Utils.Constant

object ApiCallRequest {

    @RequiresApi(Build.VERSION_CODES.O)
    fun isVoiceSend(
        isFileUploaded: String?,
        isClickType: Int,
        selectedDates: List<String>,
        isStartTimeText: String,
        isEndTimeText: String,
        title: String,
        isEmergency: Int,
        isScheduleCall: Boolean,
        schoolId: MutableList<Int>,
        targetType: Int,
        circularType: String
    ): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("voice_link", isFileUploaded)
        jsonObject.addProperty("target_type", targetType)
        jsonObject.addProperty("circular_type", circularType)
        jsonObject.addProperty(
            "duration", Constant.getAudioDurationInMinutes(isFileUploaded.toString())
        )

        val startTime: String
        val endTime: String
        if (isClickType == 2) {
            startTime = isStartTimeText
            endTime = isEndTimeText
        } else {
            startTime = Constant.getCurrentTime()
            endTime = Constant.getCurrentTime()
        }

        jsonObject.addProperty("description", title)
        jsonObject.addProperty("is_emergency", isEmergency)
        jsonObject.addProperty("is_schedule", isScheduleCall)
        jsonObject.addProperty("start_time", startTime)
        jsonObject.addProperty("end_time", endTime)
        jsonObject.addProperty("file_name", Constant.isVoiceFile)

        val jsonArray = JsonArray()
        if (isClickType == 2) {
            selectedDates.forEach { jsonArray.add(it) }
        } else {
            jsonArray.add(Constant.getCurrentDate())
        }
        jsonObject.add("schedule_date", jsonArray)

        val jsonArray1 = JsonArray()
        schoolId.forEach { jsonArray1.add(it) }
        jsonObject.add("target_code", jsonArray1)

        return jsonObject
    }

    fun isSendText(
        schoolId: String,
        message: String,
        description: String,
        targetType: Int
    ): JsonObject {
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        jsonArray.add(schoolId)

        jsonObject.add("target_code", jsonArray)
        jsonObject.addProperty("target_type", targetType)
        jsonObject.addProperty("message", message)
        jsonObject.addProperty("description", description)

        return jsonObject
    }
}