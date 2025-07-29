package com.vs.schoolmessenger.Parent.Homework.HomeWorkModelClass

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.vs.schoolmessenger.Repository.APIKeyNames
import kotlinx.parcelize.Parcelize
@Parcelize
data class GetHomeworkDetails(
    val title: String,
    val id: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val detail_id: String,
    val sent_by: String,
    var is_unread: Boolean,
    val is_completed: Boolean,
    val description: String,
    val subject_name: String,
    val file_path: List<GetFilePathDetails>
) : Parcelable