package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.ResponseKeys

data class GetHomeworkDetails(
        @SerializedName(ResponseKeys.title)
        val title:String,
        @SerializedName(ResponseKeys.description)
        val description:String,
        @SerializedName(ResponseKeys.subject_name)
        val subject_name:String,
        @SerializedName(ResponseKeys.file_path)
        val file_path: ArrayList<GetFilePathDetails>)