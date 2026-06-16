package com.vs.schoolmessenger.School.InteractionWithStudent.Response

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames
import com.vs.schoolmessenger.School.InteractionWithStudent.Model.StudentChatData

data class InteractionWithStudentResponse(
    @SerializedName(APIKeyNames.status)
    val status: Boolean,
    @SerializedName(APIKeyNames.message)
    val message: String,
    @SerializedName(APIKeyNames.data)
    val data: List<StudentChatData>
)

