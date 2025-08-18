package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotDetail( val event_name: String,
                       val event_mode: String,
                       val meeting_duration: Int,
                       val break_duration: Int,
                       val std_sec_details: List<ClassSection>,
                       val slots: List<Slot>)
