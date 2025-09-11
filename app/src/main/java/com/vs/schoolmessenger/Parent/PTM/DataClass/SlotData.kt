package com.vs.schoolmessenger.Parent.PTM.DataClass

data class SlotData( val id: String,
                     val slot_from: String,
                     val slot_to: String,
                     val is_booked: Boolean,
                     val staff_id: String,
                     val staff_name: String,
                     val subject_name: String,
                     val event_name: String,
                     val event_mode: String,
                     val event_link: String,
                     val my_booking: Boolean)
