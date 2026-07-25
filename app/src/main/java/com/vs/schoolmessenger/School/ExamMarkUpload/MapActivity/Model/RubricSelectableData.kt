package com.vs.schoolmessenger.School.ExamMarkUpload.MapActivity.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class RubricSelectableData(
    val rubric_id: String,
    val rubric_name: String?,
    val max_mark: String?,
    var selectedRubricesValue: String? = null,

    var isSelected: Boolean = false
) : Parcelable