package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class GetHomeworkData(
    @SerializedName(ResponseKeys.status)
    val status :Boolean,
    @SerializedName(ResponseKeys.message)
    val message :String,
    @SerializedName(ResponseKeys.data)
    val data: ArrayList<GetDateWiseHomeworkData>
)
