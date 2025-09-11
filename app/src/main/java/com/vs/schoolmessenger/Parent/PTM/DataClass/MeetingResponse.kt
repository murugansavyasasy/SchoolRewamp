package com.vs.schoolmessenger.Parent.PTM.DataClass

data class MeetingResponse( val status: Boolean,
                            val message: String,
                            val data: List<MeetingData>)
