package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class GetDateWiseHomeworkData(
    @SerializedName(APIKeyNames.date)
    val date: String,
    @SerializedName(APIKeyNames.homework)
    val homework: List<GetHomeworkDetails>
)