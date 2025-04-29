package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName

data class GetDateWiseHomeworkData(
    @SerializedName("date")
    val date:String,
    @SerializedName("homework")
    val homework:ArrayList<GetHomeworkDetails>
)