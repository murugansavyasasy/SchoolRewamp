package com.vs.schoolmessenger.School.PTM.DataClass

import java.io.Serializable

data class SlotDetail(
    val date: String,
    val event_name: String,
    val event_mode: String,
    val meeting_duration: Int,
    val break_duration: String,
    val start_time: String,
    val end_time: String,
    val profiles: List<Any>,
    val slots: List<Slot>,
    val std_sec_details: List<ClassSection>
) : Serializable
