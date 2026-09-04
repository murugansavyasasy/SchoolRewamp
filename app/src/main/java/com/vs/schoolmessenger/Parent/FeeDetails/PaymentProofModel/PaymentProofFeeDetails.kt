package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class PaymentProofFeeDetails(
    val term: List<Term>,
    val others: List<GenericFee>,
    val carryover: List<GenericFee>,
    val transport: List<GenericFee>,
    val hostel: List<GenericFee>,
    val quantity_result: List<GenericFee>,
    val quantity: List<Quantity>
)


