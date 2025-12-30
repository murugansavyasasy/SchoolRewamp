package com.vs.schoolmessenger.School.ExamMarkUpload.UploadMarkSheet.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UploadMarkResponse(
    val status: Boolean,
    val message: String,
    val data: ParcelTableData
) : Parcelable


