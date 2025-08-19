package com.vs.schoolmessenger.Parent.CertificateRequest

data class CertificateListData(
    val url: String,
    val type: String,
    val reason: String,
    val urgency_level: String,
    val requested_on: String,
    val status: String,
    val issued_on: String,
)
