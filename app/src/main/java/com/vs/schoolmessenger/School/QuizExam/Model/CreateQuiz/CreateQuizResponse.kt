package com.vs.schoolmessenger.School.QuizExam.Model.CreateQuiz

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

class CreateQuizResponse(
    @SerializedName(APIKeyNames.status) val status: Boolean,
    @SerializedName(APIKeyNames.message) val message: String,
    @SerializedName(APIKeyNames.data) val data: List<Any>
)