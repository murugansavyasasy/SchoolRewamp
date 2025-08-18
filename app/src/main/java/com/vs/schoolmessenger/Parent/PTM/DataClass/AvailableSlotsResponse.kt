package com.vs.schoolmessenger.Parent.PTM.DataClass

data class AvailableSlotsResponse(  val status: Boolean,
                                    val message: String,
                                    val data: List<AvailableSlot>)
