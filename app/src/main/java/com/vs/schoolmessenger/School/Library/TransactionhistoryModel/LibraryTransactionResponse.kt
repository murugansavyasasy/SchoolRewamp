package com.vs.schoolmessenger.School.Library.TransactionhistoryModel

import com.google.gson.annotations.SerializedName

data class LibraryTransactionResponse(

    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<LibraryTransactionData>
)