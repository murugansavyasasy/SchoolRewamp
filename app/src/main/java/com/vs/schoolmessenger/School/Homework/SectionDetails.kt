package com.vs.schoolmessenger.School.Homework

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SectionDetails(
    val title: String,
    val description: String
) : Parcelable
