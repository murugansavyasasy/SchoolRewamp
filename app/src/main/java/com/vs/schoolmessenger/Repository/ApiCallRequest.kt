package com.vs.schoolmessenger.Repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.vs.schoolmessenger.Utils.Constant
import com.vs.schoolmessenger.Utils.FileItem

object ApiCallRequest {

    @RequiresApi(Build.VERSION_CODES.O)
    fun isVoiceSend(
        isAcademicYearId: Int,
        isClickType: Int,
        selectedDates: List<String>,
        isStartTimeText: String,
        isEndTimeText: String,
        title: String,
        isEmergency: Boolean,
        isScheduleCall: Boolean,
        schoolId: MutableList<String>,
        targetType: Int,
        circularType: String,
        fileName: String
    ): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.addProperty(APIKeyNames.voice_link, Constant.isAwsUploadedFiles[0].isFileUrl)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.circular_type, circularType)
        jsonObject.addProperty(
            APIKeyNames.duration, Constant.getAudioDurationInSeconds(Constant.isAwsUploadedFiles[0].isFileUrl.toString())
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
        subjectId: Int): JsonObject {

        val jsonObject = JsonObject()
        val sectionArray = JsonArray()
        selectedIds.forEach { sectionArray.add(it) }
        val filePathArray = JsonArray()

        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.path, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType.toString())
            filePathArray.add(isSelectedObject)
        }

        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.section_code, sectionArray)
        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.addProperty(
            APIKeyNames.subject_id,
            subjectId.toString()
        )
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        return jsonObject
    }


//    fun isSendNotice(
//        istitle: Int,
//        iscontent: MutableList<String>,
//        istargetcode: String,
//        isintendedfor: String,
//        isvisiblefrom: String,
//        isvisibleto: String,
//        subjectId: Int): JsonObject {
//        val jsonObject = JsonObject()
//        val filePathArray = JsonArray()
//
//
//        jsonObject.addProperty(APIKeyNames.title, istitle)
//        jsonObject.addProperty(APIKeyNames.content, iscontent)
//        jsonObject.addProperty(APIKeyNames.target_code, istargetcode)
//        jsonObject.addProperty(APIKeyNames.intended_for, isintendedfor)
//        jsonObject.addProperty(APIKeyNames.visible_from, isvisiblefrom)
//        jsonObject.addProperty(APIKeyNames.visible_to,isvisibleto)
//        jsonObject.add(APIKeyNames.file_path, filePathArray)
//        return jsonObject
//    }


}