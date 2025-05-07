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
        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.addProperty(APIKeyNames.voice_link, isFileUploaded)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.circular_type, circularType)
        jsonObject.addProperty(
            APIKeyNames.duration, Constant.getAudioDurationInSeconds(isFileUploaded.toString())
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

        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.is_emergency, isEmergency)
        jsonObject.addProperty(APIKeyNames.is_schedule, isScheduleCall)
        jsonObject.addProperty(APIKeyNames.start_time, startTime)
        jsonObject.addProperty(APIKeyNames.end_time, endTime)
        jsonObject.addProperty(APIKeyNames.file_name, fileName)

        val jsonArray = JsonArray()
        if (isClickType == 2) {
            selectedDates.forEach { jsonArray.add(it) }
        } else {
            jsonArray.add(Constant.getCurrentDate())
        }
        jsonObject.add(APIKeyNames.schedule_date, jsonArray)

        val jsonArray1 = JsonArray()
        schoolId.forEach { jsonArray1.add(it) }
        jsonObject.add(APIKeyNames.target_code, jsonArray1)

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
        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.target_code, jsonArray)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.title, message)
        jsonObject.addProperty(APIKeyNames.content, description)

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
            addProperty(APIKeyNames.path, file_path)
            addProperty(APIKeyNames.type, type)
        }
        filePathArray.add(fileObject)
        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.section_code, sectionArray)
        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.addProperty(APIKeyNames.subject_id, subjectId.toString()) // convert to string as per API
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        return jsonObject
    }

}