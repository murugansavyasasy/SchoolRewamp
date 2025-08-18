package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotBookingResponse( val status: Boolean,
                                val message: String,
                                val data: List<SlotBooking>)
