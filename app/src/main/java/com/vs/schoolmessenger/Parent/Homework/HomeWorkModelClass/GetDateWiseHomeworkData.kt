package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class GetDateWiseHomeworkData(
    @SerializedName(ResponseKeys.date)
    val date:String,
    @SerializedName(ResponseKeys.homework)
    val homework:ArrayList<GetHomeworkDetails>
)