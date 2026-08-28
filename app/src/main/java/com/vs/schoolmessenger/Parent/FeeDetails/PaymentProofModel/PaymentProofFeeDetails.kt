package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

data class PaymentProofFeeDetails (
    val term: List<Term>?,
    val others: List<Any>?,
    val carryover: List<Any>?,
    val transport: List<Any>?,
    val hostel: List<Any>?,
    val quantity_result: List<Any>?,
    val quantity: List<Quantity>?
)