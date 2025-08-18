package com.vs.schoolmessenger.School.PTM.DataClass

data class SlotValidationResponse(    val status: Boolean,
                                      val message: String,
                                      val data: List<ValidatedSlot>)
