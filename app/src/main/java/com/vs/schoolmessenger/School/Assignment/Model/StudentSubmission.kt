package com.vs.schoolmessenger.School.Assignment.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class StudentSubmission (
    val student_id: String,
    val student_name: String,
    val standard: String,
    val section: String,
    val submit_status: String,
    val is_archive: Boolean,
    val submissions_details: List<SubmissionDetail>
) : Parcelable