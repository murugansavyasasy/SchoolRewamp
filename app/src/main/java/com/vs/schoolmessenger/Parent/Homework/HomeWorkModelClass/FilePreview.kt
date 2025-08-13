package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilePreview(
    val id: String,
    val title: String,
    val description: String,
    val subjectName: String? = null,
    val sentBy: String? = null,
    val thumbnail: String? = null,
    val isUnread: Boolean = false,
    val isCompleted: Boolean = false,
    val isMenuType: Int? = null,
    val submittedCount: Int? = null,
    val totalCount: Int? = null,
    val assignmentid: String? = null,
    val created_date: String? = null,
    val subject_name: String? = null,
    val category: String? = null,
    val assignmentsubject: String? = null,
    val isParentAssignment: Boolean? = null,
    val fileList: List<GetFilePathDetails> = emptyList(),
): Parcelable