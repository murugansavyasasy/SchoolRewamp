package com.vs.schoolmessenger.Parent.FeeDetails.Model

import com.google.gson.annotations.SerializedName

data class FeeInvoiceResponse(
    @SerializedName("status")
    val status: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<InvoiceData>
) {
    data class InvoiceData(
        @SerializedName("id")
        val id: String,

        @SerializedName("invoice_no")
        val invoice_no: String,

        @SerializedName("invoice_date")
        val invoice_date: String,

        @SerializedName("invoice_amount")
        val invoice_amount: String
    )
}


