package com.vs.schoolmessenger.School.DailyCollection.FeeDiscount.Model.DiscountReport

import com.google.gson.annotations.SerializedName

data class DiscountItem(
    @SerializedName("student_name") val studentName: String? = null,
    @SerializedName("admission_no") val admissionNo: String? = null,
    @SerializedName("class_name") val className: String? = null,
    @SerializedName("section_name") val sectionName: String? = null,
    @SerializedName("fee_group_name") val feeGroupName: String? = null,
    @SerializedName("fee_name") val feeName: String? = null,
    @SerializedName("given_date") val givenDate: String? = null,
    @SerializedName("given_by") val givenBy: String? = null,
    @SerializedName("discount_used") val discountUsed: String? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("amount") val amount: String? = null
)