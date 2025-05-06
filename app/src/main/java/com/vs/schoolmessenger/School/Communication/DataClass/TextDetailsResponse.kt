package com.vs.schoolmessenger.School.Communication.DataClass

data class TextDetailsResponse(    val status: Boolean,
                                   val message: String,
                                   val data: List<TextDetail>)
