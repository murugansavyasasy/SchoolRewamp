package com.vs.schoolmessenger.Parent.PTM.DataClass

data class MeetingItem(val id: String,
                       val date: String,
                       val time: String,
                       val status: String,
                       val purpose: String,
                       val mode: String,
                       val event_link: String,
                       val staff_id: String,
                       val staff_name: String,
                       val subject_name: String,
                       val staff_phone: String?)
