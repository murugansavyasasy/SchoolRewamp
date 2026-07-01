package com.vs.schoolmessenger.Parent.ClassTestMark.DataClass

import com.google.gson.annotations.SerializedName

data class ClassTestMarkResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<MarkData>
)