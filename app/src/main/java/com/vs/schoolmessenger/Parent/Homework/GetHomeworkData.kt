package com.vs.schoolmessenger.Parent.Homework

import com.google.gson.annotations.SerializedName

data class GetHomeworkData(
    @SerializedName("status")
    val status :Boolean,
    @SerializedName("message")
    val message :String,
    @SerializedName("data")
    val data: ArrayList<GetDateWiseHomeworkData>
){
    data class GetDateWiseHomeworkData(
        @SerializedName("date")
        val date:String,
        @SerializedName("homework")
        val homework:ArrayList<GetHomeworkDetails>
    )
    {
        data class GetHomeworkDetails(
            @SerializedName("title")
            val title:String,
            @SerializedName("description")
            val description:String,
            @SerializedName("subject_name")
            val subject_name:String,
            @SerializedName("file_path")
            val filePath: ArrayList<GetFilePathDetails>
        )
        {
            data class GetFilePathDetails(
                @SerializedName("type")
                val type: String,
                @SerializedName("path")
                val path: String,
            )
        }
    }
}
