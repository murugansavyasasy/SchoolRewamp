package com.vs.schoolmessenger.School.PTM.DataClass

import android.os.Parcelable
import com.vs.schoolmessenger.School.PTM.Adapter.SelectedClassSection
import kotlinx.parcelize.Parcelize

@Parcelize
data class MeetingCreationData(
    val purpose: String,
    val meetingMode: String,
    val meetingLink: String?,
    val selectedSections: MutableList<SelectedClassSection>,
    val selectedDates: List<String>,
    val fromTime: String,
    val toTime: String,
    val slotDuration: String,
    val slotsCount: String?,
    val break_time: String?
) : Parcelable
