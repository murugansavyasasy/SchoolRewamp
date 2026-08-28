package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class Term(
    val term_name: String?,
    val term_id: String?,
    val term_group_type_id: String?,
    val fees_details: List<FeesDetails>?
)