package com.vs.schoolmessenger.School.Library.FineAmountReportModel

import com.google.gson.annotations.SerializedName

data class LibraryFineAmountData (
    @SerializedName("closure_id")
    val closureId: String,

    @SerializedName("receipt_id")
    val receiptId: String,

    @SerializedName("student_id")
    val studentId: String,

    @SerializedName("staff_id")
    val staffId: String,

    @SerializedName("adm_no_emp_id")
    val admNoEmpId: String,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("staff_name")
    val staffName: String,

    @SerializedName("class_name")
    val className: String,

    @SerializedName("section_name")
    val sectionName: String,

    @SerializedName("book_no")
    val bookNo: String,

    @SerializedName("book_name")
    val bookName: String,

    @SerializedName("barcode_number")
    val barcodeNumber: String,

    @SerializedName("paid_date")
    val paidDate: String,

    @SerializedName("fine_amount")
    val fineAmount: String,

    @SerializedName("discount_amount")
    val discountAmount: String
)