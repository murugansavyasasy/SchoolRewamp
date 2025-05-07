package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class GetHomeworkDetails(
        @SerializedName(APIKeyNames.title)
        val title:String,
        @SerializedName(APIKeyNames.description)
        val description:String,
        @SerializedName(APIKeyNames.subject_name)
        val subject_name:String,
        @SerializedName(APIKeyNames.file_path)
        val file_path: ArrayList<GetFilePathDetails>)