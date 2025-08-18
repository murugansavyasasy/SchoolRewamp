package com.vs.schoolmessenger.School.PTM.DataClass

data class ValidatedSlot(  val institute_id: String,
                           val staff_id: String,
                           val break_time: Int,
                           val date: String,
                           val duration: Int,
                           val event_name: String,
                           val meeting_mode: String,
                           val from_time: String,
                           val to_time: String,
                           val slots: List<SlotAvailability>,
                           val std_sec_details: List<ClassSectionDetail>)
