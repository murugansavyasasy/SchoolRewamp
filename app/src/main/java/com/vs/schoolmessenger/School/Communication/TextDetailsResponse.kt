package com.vs.schoolmessenger.School.Communication

data class TextDetailsResponse(    val status: Boolean,
                                   val message: String,
                                   val data: List<TextDetail>)
