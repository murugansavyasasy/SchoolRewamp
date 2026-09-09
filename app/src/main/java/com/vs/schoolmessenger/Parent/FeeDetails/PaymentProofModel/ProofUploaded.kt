package com.vs.schoolmessenger.Parent.FeeDetails.PaymentProofModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProofUploaded(
    val url: String?,
    val type: String,
    val file_name: String?,
    val original_file_name: String?
): Parcelable
