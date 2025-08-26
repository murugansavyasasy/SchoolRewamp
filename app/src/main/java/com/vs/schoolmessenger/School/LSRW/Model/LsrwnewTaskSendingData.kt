package com.vs.schoolmessenger.School.LSRW.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class LsrwnewTaskSendingData (
    val isTitle: String,
    val isDescription: String,
    val isLsrwType: String,
    val submission_date: String,

) : Parcelable