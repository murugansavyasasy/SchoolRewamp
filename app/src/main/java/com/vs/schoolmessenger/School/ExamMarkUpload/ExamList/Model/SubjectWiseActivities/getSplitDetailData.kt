package com.vs.schoolmessenger.School.ExamMarkUpload.ExamList.Model.SubjectWiseActivities

import android.os.Parcelable


import kotlinx.parcelize.Parcelize

@Parcelize
data class getSplitDetailData(
    val id: String,
    val name: String
) : Parcelable