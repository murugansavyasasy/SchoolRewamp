package com.vs.schoolmessenger.Parent.CertificateRequest

data class CertificatesListResponse(
    val status: Boolean,
    val message: String,
    val data: List<CertificateListData>
)
