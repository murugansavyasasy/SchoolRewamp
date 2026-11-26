package com.vs.schoolmessenger.School.Library.TransactionhistoryModel

import com.google.gson.annotations.SerializedName

data class LibraryTransactionData(

    @SerializedName("issue_id")
    val issueId: String,

    @SerializedName("book_id")
    val bookId: String,

    @SerializedName("book_no")
    val bookNo: String,

    @SerializedName("book_name")
    val bookName: String,

    @SerializedName("author")
    val author: String,

    @SerializedName("barcode_number")
    val barcodeNumber: String,

    @SerializedName("created_on")
    val createdOn: String,

    @SerializedName("adm_no_emp_id")
    val admNoEmpId: String,

    @SerializedName("issued_to")
    val issuedTo: String,

    @SerializedName("issued_to_type")
    val issuedToType: String,

    @SerializedName("student_name")
    val studentName: String,

    @SerializedName("class_name")
    val className: String,

    @SerializedName("section_name")
    val sectionName: String,

    @SerializedName("issued_on")
    val issuedOn: String,

    @SerializedName("due_date")
    val dueDate: String,

    @SerializedName("return_date")
    val returnDate: String,

    @SerializedName("renew_date")
    val renewDate: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("remarks")
    val remarks: String
)