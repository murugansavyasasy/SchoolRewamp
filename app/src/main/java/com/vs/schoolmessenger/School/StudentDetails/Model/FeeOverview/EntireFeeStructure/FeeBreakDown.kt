package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure

data class FeeBreakDown(
    val fee_group_type_id: Int?,
    val fee_group_type_name: String?,
    val fee_id: Int?,
    val fee_name: String?,
    val m_feeamount: Int?,
    val paid_amount: Int?,
    val pending_amount: Int?,
    val discount_ammount: Int?,
    val due_date: String?,
    val status: String?
)