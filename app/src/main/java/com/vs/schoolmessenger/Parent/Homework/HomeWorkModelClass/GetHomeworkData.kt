package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames

data class GetHomeworkData(
    @SerializedName(APIKeyNames.status)
    val status: Boolean,

    @SerializedName(APIKeyNames.message)
    val message: String,

    @SerializedName(APIKeyNames.data)
    val data: List<GetHomeworkDetails>
)




//package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass
//
//import com.google.gson.annotations.SerializedName
//import com.vs.schoolmessenger.Repository.APIKeyNames
//
//data class GetHomeworkData(
//
//    @SerializedName(APIKeyNames.status) val status: Boolean,
//    @SerializedName(APIKeyNames.message) val message: String,
//    @SerializedName(APIKeyNames.data) val data: List<GetDateWiseHomeworkData>
//
//)
