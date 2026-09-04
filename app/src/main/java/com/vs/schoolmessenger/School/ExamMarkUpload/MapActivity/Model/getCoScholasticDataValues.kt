package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class getCoScholasticDataValues(
    val id: String,
    val name: String?,
    val type: String?,
    val activities: List<String> = emptyList(),
    var selectedValue: String? = null,
    var isSelected: Boolean = false

) : Parcelable