package com.vs.schoolmessenger.Repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Utils.Constant

object ApiCallRequest {

    @RequiresApi(Build.VERSION_CODES.O)
    fun isVoiceSend(
        isAcademicYearId: Int,
        isFileUploaded: String?,
        isClickType: Int,
        selectedDates: List<String>,
        isStartTimeText: String,
        isEndTimeText: String,
        title: String,
        isEmergency: Int,
        isScheduleCall: Boolean,
        schoolId: MutableList<String>,
        targetType: Int,
        circularType: String,
        fileName: String
    ): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(RequestKeys.academic_year_id, isAcademicYearId)
        jsonObject.addProperty(RequestKeys.voice_link, isFileUploaded)
        jsonObject.addProperty(RequestKeys.target_type, targetType)
        jsonObject.addProperty(RequestKeys.circular_type, circularType)
        jsonObject.addProperty(
            RequestKeys.duration, Constant.getAudioDurationInSeconds(isFileUploaded.toString())
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

        jsonObject.addProperty(RequestKeys.title, title)
        jsonObject.addProperty(RequestKeys.is_emergency, isEmergency)
        jsonObject.addProperty(RequestKeys.is_schedule, isScheduleCall)
        jsonObject.addProperty(RequestKeys.start_time, startTime)
        jsonObject.addProperty(RequestKeys.end_time, endTime)
        jsonObject.addProperty(RequestKeys.file_name, fileName)

        val jsonArray = JsonArray()
        if (isClickType == 2) {
            selectedDates.forEach { jsonArray.add(it) }
        } else {
            jsonArray.add(Constant.getCurrentDate())
        }
        jsonObject.add(RequestKeys.schedule_date, jsonArray)

        val jsonArray1 = JsonArray()
        schoolId.forEach { jsonArray1.add(it) }
        jsonObject.add(RequestKeys.target_code, jsonArray1)

        return jsonObject
    }

    fun isSendText(
        isAcademicYearId: Int,
        schoolId: MutableList<String>,
        message: String,
        description: String,
        targetType: Int
    ): JsonObject {
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        schoolId.forEach { jsonArray.add(it) }
        jsonObject.addProperty(RequestKeys.academic_year_id, isAcademicYearId)
        jsonObject.add(RequestKeys.target_code, jsonArray)
        jsonObject.addProperty(RequestKeys.target_type, targetType)
        jsonObject.addProperty(RequestKeys.title, message)
        jsonObject.addProperty(RequestKeys.content, description)

        return jsonObject
    }

    fun isSendHomeWork(
        isAcademicYearId: Int,
        selectedIds: MutableList<String>,
        title: String,
        description: String,
        subjectId: Int,
        file_path: String,
        type: String
    ): JsonObject {
        val jsonObject = JsonObject()
        val sectionArray = JsonArray()
        selectedIds.forEach { sectionArray.add(it) }

        val filePathArray = JsonArray()
        val fileObject = JsonObject().apply {
            addProperty("path", file_path)
            addProperty("type", type)
        }
        filePathArray.add(fileObject)

        jsonObject.addProperty(RequestKeys.academic_year_id, isAcademicYearId)
        jsonObject.add(RequestKeys.section_code, sectionArray)
        jsonObject.addProperty(RequestKeys.title, title)
        jsonObject.addProperty(RequestKeys.description, description)
        jsonObject.addProperty(RequestKeys.subject_id, subjectId.toString()) // convert to string as per API
        jsonObject.add("file_path", filePathArray)

        return jsonObject
    }

}