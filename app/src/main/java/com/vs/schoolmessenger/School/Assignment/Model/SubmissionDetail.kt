package com.vs.schoolmessenger.School.Assignment.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class SubmissionDetail(
    val id: String,
    val description: String,
    val submitted_on: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val file_path: List<FilePath>
) : Parcelable