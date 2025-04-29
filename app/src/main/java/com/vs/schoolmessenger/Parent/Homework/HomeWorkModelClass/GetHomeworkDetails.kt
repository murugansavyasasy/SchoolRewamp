package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName

data class GetHomeworkDetails(
        @SerializedName("title")
        val title:String,
        @SerializedName("description")
        val description:String,
        @SerializedName("subject_name")
        val subject_name:String,
        @SerializedName("file_path")
        val filePath: ArrayList<GetFilePathDetails>)