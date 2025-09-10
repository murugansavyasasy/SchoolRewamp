package com.vs.schoolmessenger.Parent.PTM.DataClass

class MeetingData( val staff_id: String,
                   val staff_name: String,
                   val event_name: String,
                   val subject_name: String,
                   val start_time: String,
                   val end_time: String,
                   val slots: List<SlotData>)