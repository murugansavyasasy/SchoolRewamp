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
        isCommunicationType: Int,
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
        if (isCommunicationType == 2) {
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
        if (isCommunicationType == 2) {
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
        targetType: Int,
        iframe: String,
        file_size: String,
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
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType.toString())
            filePathArray.add(isSelectedObject)
        }

        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.target_code, sectionArray)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.iframe, iframe)
        jsonObject.addProperty(APIKeyNames.file_size, file_size)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.addProperty(
            APIKeyNames.subject_id,
            subjectId.toString()
        )
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        return jsonObject
    }

    fun isSendAssignment(
        targetType: Int,
        iframe: String,
        file_size: String,
        isAcademicYearId: Int,
        selectedIds: MutableList<String>,
        title: String,
        description: String,
        assignmentType: String,
        date: String,
        time: String,
        subjectId: Int
    ): JsonObject {

        val jsonObject = JsonObject()
        val sectionArray = JsonArray()
        selectedIds.forEach { sectionArray.add(it) }
        val filePathArray = JsonArray()

        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type,
                Constant.isAwsUploadedFiles[i].isFileType.toString()
            )
            filePathArray.add(isSelectedObject)
        }

        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.target_code, sectionArray)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty("submission_date", date)
        jsonObject.addProperty("category", assignmentType)
        jsonObject.addProperty(APIKeyNames.time, time)
        jsonObject.addProperty(APIKeyNames.iframe, iframe)
        jsonObject.addProperty(APIKeyNames.file_size, file_size)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.addProperty(
            APIKeyNames.subject_id,
            subjectId.toString()
        )
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        return jsonObject
    }

    fun isSendAttachment(
        isAcademicYearId: Int,
        selectedIds: MutableList<String>,
        title: String,
        description: String,
        targetType: Int,
        iframe: String,
        fileSize: String,
    ): JsonObject {

        val jsonObject = JsonObject()
        val sectionArray = JsonArray()
        selectedIds.forEach { sectionArray.add(it) }
        val filePathArray = JsonArray()

        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(
                APIKeyNames.type,
                Constant.isAwsUploadedFiles[i].isFileType.toString()
            )
            filePathArray.add(isSelectedObject)
        }

        jsonObject.addProperty(APIKeyNames.academic_year_id, isAcademicYearId)
        jsonObject.add(APIKeyNames.target_code, sectionArray)
        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.target_type, targetType)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.addProperty(APIKeyNames.iframe, iframe)
        jsonObject.addProperty(APIKeyNames.file_size, fileSize)
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        return jsonObject
    }

    fun isSendNotice(
        title: String,
        description: String,
        startDate: String,
        endDate: String,
        target_code: MutableList<String>,
        intended_for: String,
        iframe: String,
        fileSize: String
    ): JsonObject {
        val jsonObject = JsonObject()
        val filePathArray = JsonArray()

        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType.toString())
            filePathArray.add(isSelectedObject)
        }

        val targetCodeArray = JsonArray()
        for (code in target_code) {
            targetCodeArray.add(code)
        }

        jsonObject.addProperty("title", title)
        jsonObject.addProperty(APIKeyNames.description, description)
        jsonObject.add("target_code", targetCodeArray)
        jsonObject.addProperty("intended_for", intended_for)
        jsonObject.addProperty("visible_from", startDate)
        jsonObject.addProperty("visible_to", endDate)
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        jsonObject.addProperty(APIKeyNames.iframe, iframe)
        jsonObject.addProperty(APIKeyNames.file_size, fileSize)
        return jsonObject
    }


    fun isSendEvent(
        title: String,
        content: String,
        venue: String,
        event_date: String,
        event_time: String,
        target_type: Int?,
        target_code: MutableList<String>,
        iframe: String,
        fileSize: String
    ): JsonObject {
        val jsonObject = JsonObject()
        val filePathArray = JsonArray()

        for (i in Constant.isAwsUploadedFiles.indices) {
            val isSelectedObject = JsonObject()
            isSelectedObject.addProperty(APIKeyNames.url, Constant.isAwsUploadedFiles[i].isFileUrl)
            isSelectedObject.addProperty(APIKeyNames.type, Constant.isAwsUploadedFiles[i].isFileType.toString())
            filePathArray.add(isSelectedObject)
        }

        val targetCodeArray = JsonArray()
        for (code in target_code) {
            targetCodeArray.add(code)
        }

        jsonObject.addProperty(APIKeyNames.title, title)
        jsonObject.addProperty(APIKeyNames.description, content)
        jsonObject.add("target_code", targetCodeArray)
        jsonObject.addProperty("venue", venue)
        jsonObject.addProperty("event_date", event_date)
        jsonObject.addProperty("event_time", event_time)
        jsonObject.addProperty("target_type", target_type)
        jsonObject.add(APIKeyNames.file_path, filePathArray)
        jsonObject.addProperty(APIKeyNames.iframe, iframe)
        jsonObject.addProperty(APIKeyNames.file_size, fileSize)

        return jsonObject
    }










}