package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class getActivityPaperNameData(
    val activity_id: String,
    val name: String?,
    val max_mark: String?,

    val activities: List<String> = emptyList(),
    var selectedValue: String? = null,

    var selectedActivityID: String? = null,
    val rubrics: List<RubricSelectableData> = emptyList(),
    var isExpanded: Boolean = false
) : Parcelable