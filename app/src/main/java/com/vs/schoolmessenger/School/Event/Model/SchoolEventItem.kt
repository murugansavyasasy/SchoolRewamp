package com.vs.schoolmessenger.School.Event.Model

import android.os.Parcelable
import com.vs.schoolmessenger.Parent.EventsHolidays.EventActivty.RewampModelEvent.FilePath
import kotlinx.parcelize.Parcelize

@Parcelize
data class SchoolEventItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val date: String,
    val time: String,
    val venue: String,
    val sent_by: String,
    val iframe: String,
    val file_size: String,
    val thumbnail: String,
    val can_edit: Boolean,
    val can_delete: Boolean,
    val file_path: List<FilePath>
) : Parcelable