package com.vs.schoolmessenger.School.StudentDetails.Model.FeeOverview.EntireFeeStructure

data class FeeOverview(
    val id: Int?,
    val fee_name: String?,
    val term_name: String?,
    val term_id: Int?,
    val actual_amount: Int?,
    val paid_amount: Int?,
    val discount_amount: Int?,
    val pending_amount: Int?,
    val status: String?,
    val hostel_name: String?,
    val room_no: String?,
    val bed_no: String?,
    val breakDown: List<FeeBreakDown>?,
    val term_fees: FeeOverview?,


)